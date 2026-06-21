package uw.notify.center.service;

import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import uw.common.response.ResponseData;
import uw.common.util.JsonUtils;
import uw.common.util.SystemClock;
import uw.notify.center.conf.UwNotifyCenterProperties;
import uw.notify.center.constant.Constants;
import uw.notify.client.vo.WebNotifyMsg;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Web 通知服务，负责维护 SSE 长连接池并向在线用户投递消息。
 * <p>
 * 单实例内以 {@code userId} 为 key 维护 {@link SseEmitter} 连接池；当目标用户不在当前实例时，
 * 通过 Redis Pub/Sub（{@link Constants#REDIS_NOTIFY_CHANNEL}）广播到全部实例，
 * 由持有该用户连接的实例完成最终投递。
 * <p>
 * 设计要点：
 * <ul>
 *     <li>连接池以 {@code userId} 为单维 key，不支持按 saasId 维度的广播；
 *     {@code userId<=0} 的消息会被直接拒绝，避免误入 Redis 广播放大流量。</li>
 *     <li>独立的心跳调度器周期性对超过心跳间隔未发送过数据的连接发送 SSE 注释帧，
 *     防止反向代理 / 负载均衡器因空闲超时断开连接。</li>
 *     <li>清理采用 CAS（{@code remove(key,value)}），避免重连后误移除新连接。</li>
 * </ul>
 *
 * @author axeon
 */
@Service
public class WebNotifyService {

    /**
     * 心跳检查最小间隔（毫秒），防止 {@code sseHeartbeatInterval} 配置为 0/负值导致 CPU 空转，
     * 同时作为运维调小心跳间隔的下限保护。
     */
    private static final long MIN_CHECK_INTERVAL = 10_000L;

    /**
     * 日志。
     */
    private static final Logger log = LoggerFactory.getLogger(WebNotifyService.class);

    /**
     * SSE 连接池：{@code userId → 对应的 SSE 连接包装器}。
     * <p>
     * 单实例下每个用户最多保留一条连接；同 userId 再次建连会踢掉旧连接。
     */
    private static final ConcurrentHashMap<Long, SseEmitterWrapper> sseEmitterMap = new ConcurrentHashMap<>();

    /**
     * notify-center 配置（含 SSE 超时、心跳间隔、Redis 连接参数）。
     */
    private static UwNotifyCenterProperties uwNotifyCenterProperties;

    /**
     * 用于发布跨实例广播消息的 Redis 模板。
     */
    private static RedisTemplate<String, String> notifyRedisTemplate;

    /**
     * 心跳调度器，应用关闭时由 {@link #shutdown()} 优雅停止。
     */
    private static ScheduledExecutorService heartbeatExecutor;

    /**
     * 构造方法，由 Spring 完成静态字段注入并启动心跳调度。
     *
     * @param uwNotifyCenterProperties notify-center 配置
     * @param notifyRedisTemplate      用于发布跨实例广播的 Redis 模板
     */
    public WebNotifyService(UwNotifyCenterProperties uwNotifyCenterProperties, RedisTemplate<String, String> notifyRedisTemplate) {
        WebNotifyService.uwNotifyCenterProperties = uwNotifyCenterProperties;
        WebNotifyService.notifyRedisTemplate = notifyRedisTemplate;
        startHeartbeat();
    }

    /**
     * 启动心跳调度器，周期性遍历所有连接，仅对超过心跳间隔未发送过数据的连接发送 SSE 注释帧保活。
     * <p>
     * 实际心跳间隔与检查间隔均不低于 {@link #MIN_CHECK_INTERVAL}，避免配置过小导致 CPU 空转。
     */
    private static void startHeartbeat() {
        long interval = Math.max(uwNotifyCenterProperties.getSseHeartbeatInterval(), MIN_CHECK_INTERVAL);
        long checkInterval = Math.max(interval / 2, MIN_CHECK_INTERVAL);
        heartbeatExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "sse-heartbeat");
            t.setDaemon(true);
            return t;
        });
        heartbeatExecutor.scheduleAtFixedRate(() -> {
            long now = SystemClock.now();
            for (Map.Entry<Long, SseEmitterWrapper> entry : sseEmitterMap.entrySet()) {
                SseEmitterWrapper wrapper = entry.getValue();
                if (now - wrapper.lastSendTime < interval) {
                    continue;
                }
                try {
                    wrapper.send(SseEmitter.event().comment("heartbeat"));
                } catch (Exception e) {
                    log.warn("心跳失败, userId={}, error={}", entry.getKey(), e.getMessage());
                    cleanup(entry.getKey(), wrapper);
                }
            }
        }, checkInterval, checkInterval, TimeUnit.MILLISECONDS);
    }

    /**
     * 应用关闭时停止心跳调度器，等待在途任务结束。
     */
    @PreDestroy
    public void shutdown() {
        if (heartbeatExecutor != null) {
            heartbeatExecutor.shutdownNow();
            try {
                heartbeatExecutor.awaitTermination(2, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * 向指定用户投递通知。
     * <p>
     * 当前架构下 SSE 连接池以 {@code userId} 为单维 key，不支持按 saasId 维度广播，
     * 因此 {@code userId<=0}（广播语义）将被直接拒绝，避免误入 Redis 广播放大流量。
     * <p>
     * 跨实例转发仅在 {@code autoRelay=true} 且 {@code clusterMode=true}（多机模式）时触发；
     * 单机模式下本地未命中即视为不在线，不发起广播。
     *
     * @param webNotifyMsg 通知消息体（{@code userId} 必须 &gt; 0）
     * @param autoRelay    本地不在线时是否允许跨实例转发；
     *                     {@code true}=RPC 入口，允许在多机模式下走 Redis 广播；
     *                     {@code false}=订阅回调入口，仅本地投递，避免循环广播
     * @return 投递结果：
     *         <ul>
     *           <li>{@link ResponseData#SUCCESS}：本地投递成功；</li>
     *           <li>{@link ResponseData#WARN}：已转发至 Redis 广播通道（多机模式 + 本地不在线 + 允许转发）；</li>
     *           <li>其他：目标非法、或本地不在线且未进入转发。</li>
     *         </ul>
     */
    public static ResponseData pushMsg(WebNotifyMsg webNotifyMsg, boolean autoRelay) {
        if (webNotifyMsg == null || webNotifyMsg.getUserId() <= 0) {
            return ResponseData.errorMsg("无效的通知目标用户！");
        }
        SseEmitterWrapper se = sseEmitterMap.get(webNotifyMsg.getUserId());
        if (se == null && autoRelay && uwNotifyCenterProperties.isClusterMode()) {
            //本地不在线、开启自动转发且为多机集群模式时，通过 Redis Pub/Sub 广播到其他实例。
            //单机模式（clusterMode=false）下不广播，直接视为用户不在线，避免自发自收的无谓处理。
            notifyRedisTemplate.convertAndSend(Constants.REDIS_NOTIFY_CHANNEL, JsonUtils.toString(webNotifyMsg));
            return ResponseData.WARN;
        }
        if (se == null) {
            return ResponseData.errorMsg("指定用户[" + webNotifyMsg.getUserId() + "]不在线！");
        }
        try {
            se.send(webNotifyMsg.getNotifyBody(), MediaType.APPLICATION_JSON);
            return ResponseData.SUCCESS;
        } catch (Exception e) {
            log.error("指定用户[{}]发送信息出错：{}", webNotifyMsg.getUserId(), e.getMessage(), e);
            cleanup(webNotifyMsg.getUserId(), se);
            // 对外用固定文案，避免泄露内部异常细节（符合序列化异常对外报错安全规范）。
            return ResponseData.errorMsg("推送通知失败。");
        }
    }

    /**
     * 返回当前实例所有在线用户 ID 的不可变快照。
     *
     * @return 在线用户 ID 列表（不可变）
     */
    public static List<Long> getOnlineIds() {
        return Collections.unmodifiableList(new ArrayList<>(sseEmitterMap.keySet()));
    }

    /**
     * 返回当前实例在线连接数。
     *
     * @return 在线连接数
     */
    static int getOnlineCount() {
        return sseEmitterMap.size();
    }

    /**
     * 为指定用户建立 SSE 连接。
     * <p>
     * 注册完成 / 错误 / 超时回调统一走 {@link #cleanup} 清理；若该 userId 已存在旧连接，先关闭旧的再替换。
     *
     * @param userId 用户 ID（必须 &gt; 0）
     * @return 已注册回调的 {@link SseEmitterWrapper}
     */
    public static SseEmitter openStream(long userId) {
        // 设置超时日期，0表示不过期
        SseEmitterWrapper wrapper = new SseEmitterWrapper(uwNotifyCenterProperties.getSseTimeout());
        // 注册回调
        wrapper.onCompletion(() -> cleanup(userId, wrapper));
        wrapper.onError(throwable -> {
            cleanup(userId, wrapper);
            log.error("SSE错误:{}", throwable.getMessage(), throwable);
        });
        wrapper.onTimeout(() -> cleanup(userId, wrapper));
        // 关闭旧连接
        SseEmitterWrapper oldEmitter = sseEmitterMap.put(userId, wrapper);
        if (oldEmitter != null) {
            try {
                oldEmitter.complete();
            } catch (Exception ignored) {
            }
        }
        return wrapper;
    }

    /**
     * 清理指定 Emitter 的连接。
     * <p>
     * 仅当 Map 中存的仍是同一个 Emitter 时才移除（CAS），避免误清理重连后的新连接。
     *
     * @param userId  用户 ID
     * @param emitter 待清理的 Emitter
     */
    private static void cleanup(long userId, SseEmitterWrapper emitter) {
        if (sseEmitterMap.remove(userId, emitter)) {
            try {
                emitter.complete();
            } catch (Exception ignored) {
            }
        }
        if (log.isTraceEnabled()) {
            log.trace("remove user id:{}, remain online count:{}. ", userId, sseEmitterMap.size());
        }
    }

    /**
     * {@link SseEmitter} 包装类，额外携带最后一次成功发送时间，供心跳保活判断使用。
     */
    static class SseEmitterWrapper extends SseEmitter {

        /**
         * 最后一次成功发送数据的时间戳（{@link SystemClock#now()}），0 表示尚未发送过。
         */
        volatile long lastSendTime = 0;

        /**
         * @param timeout SSE 连接超时（毫秒），0 表示不过期
         */
        SseEmitterWrapper(long timeout) {
            super(timeout);
        }

        /**
         * 发送数据并刷新最后发送时间。
         *
         * @param data 要发送的数据
         * @throws IOException 发送失败时抛出
         */
        @Override
        public void send(Object data) throws IOException {
            super.send(data);
            lastSendTime = SystemClock.now();
        }

        /**
         * 发送指定 MediaType 的数据并刷新最后发送时间。
         *
         * @param data      要发送的数据
         * @param mediaType 数据的 MediaType
         * @throws IOException 发送失败时抛出
         */
        @Override
        public void send(Object data, MediaType mediaType) throws IOException {
            super.send(data, mediaType);
            lastSendTime = SystemClock.now();
        }

        /**
         * 发送 SSE 事件（如心跳注释帧）并刷新最后发送时间。
         *
         * @param builder SSE 事件构造器
         * @throws IOException 发送失败时抛出
         */
        @Override
        public void send(SseEventBuilder builder) throws IOException {
            super.send(builder);
            lastSendTime = SystemClock.now();
        }
    }

}
