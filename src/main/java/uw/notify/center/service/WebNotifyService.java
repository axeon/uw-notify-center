package uw.notify.center.service;

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

/**
 * 通知服务。
 */
@Service
public class WebNotifyService {

    /**
     * 日志。
     */
    private static final Logger log = LoggerFactory.getLogger(WebNotifyService.class);

    /**
     * SSE连接池。
     */
    private static final ConcurrentHashMap<Long, SseEmitterWrapper> sseEmitterMap = new ConcurrentHashMap<>();


    /**
     * Redis配置。
     */
    private static UwNotifyCenterProperties uwNotifyCenterProperties;

    /**
     * Redis模板。
     */
    private static RedisTemplate<String, String> notifyRedisTemplate;

    public WebNotifyService(UwNotifyCenterProperties uwNotifyCenterProperties, RedisTemplate<String, String> notifyRedisTemplate) {
        WebNotifyService.uwNotifyCenterProperties = uwNotifyCenterProperties;
        WebNotifyService.notifyRedisTemplate = notifyRedisTemplate;
        startHeartbeat();
    }

    /**
     * 启动心跳线程，定期遍历所有连接，仅对超过心跳间隔未发送过消息的连接发送SSE注释帧保活。
     */
    private static void startHeartbeat() {
        long interval = uwNotifyCenterProperties.getSseHeartbeatInterval();
        long checkInterval = interval / 2;
        Thread thread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(checkInterval);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
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
            }
        }, "sse-heartbeat");
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * 给指定用户发信息
     */
    public static ResponseData pushMsg(WebNotifyMsg webNotifyMsg, boolean autoRelay) {
        SseEmitterWrapper se = sseEmitterMap.get(webNotifyMsg.getUserId());
        if (se == null && autoRelay) {
            //如果本地不在线，并且开启了自动转发，则在redis中广播发送。
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
            return ResponseData.errorMsg(e.getMessage());
        }
    }

    /**
     * 获取ID列表。
     *
     * @return
     */
    public static List<Long> getOnlineIds() {
        return Collections.unmodifiableList(new ArrayList<>(sseEmitterMap.keySet()));
    }

    /**
     * 获取在线数。
     *
     * @return
     */
    static int getOnlineCount() {
        return sseEmitterMap.size();
    }

    /**
     * 构造timeout。
     *
     * @param userId
     * @return
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
     * 清理指定Emitter的连接。
     * 仅当Map中存的仍是同一个Emitter时才移除，避免误清理重连后的新连接。
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
     * 包装SseEmitter，携带最后发送时间。
     */
    static class SseEmitterWrapper extends SseEmitter {

        /**
         * 最后发送时间。
         */
        volatile long lastSendTime = 0;

        SseEmitterWrapper(long timeout) {
            super(timeout);
        }

        @Override
        public void send(Object data) throws IOException {
            super.send(data);
            lastSendTime = SystemClock.now();
        }

        @Override
        public void send(Object data, MediaType mediaType) throws IOException {
            super.send(data, mediaType);
            lastSendTime = SystemClock.now();
        }

        @Override
        public void send(SseEventBuilder builder) throws IOException {
            super.send(builder);
            lastSendTime = SystemClock.now();
        }
    }

}