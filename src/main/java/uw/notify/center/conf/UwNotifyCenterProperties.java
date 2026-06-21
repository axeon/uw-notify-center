package uw.notify.center.conf;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * notify-center 配置类，前缀 {@code uw.notify.center}。
 * <p>
 * 涵盖 SSE 连接超时、心跳间隔，以及独立的 Redis 连接参数（用于跨实例广播）。
 *
 * @author axeon
 */
@Configuration
@ConfigurationProperties(prefix = "uw.notify.center")
public class UwNotifyCenterProperties {

    /**
     * SSE 连接超时时间（毫秒），默认 60 秒。
     * <p>
     * 超过后连接将被服务端关闭；配置为 0 表示不过期（不推荐，需配合客户端重连）。
     */
    private long sseTimeout = 60_000L;

    /**
     * SSE 心跳间隔（毫秒），默认 30 秒。
     * <p>
     * 心跳调度器周期性对超过该间隔未发送过数据的连接发送 SSE 注释帧保活，
     * 防止反向代理 / 负载均衡器因空闲超时断开连接。
     * 实际生效值不低于 {@code WebNotifyService.MIN_CHECK_INTERVAL}（10 秒）。
     */
    private long sseHeartbeatInterval = 30_000L;

    /**
     * notify-center 专用 Redis 连接配置（host/port/lettuce 连接池等）。
     */
    private RedisProperties redis = new RedisProperties();

    /**
     * 是否多机集群模式，默认 {@code true}。
     * <p>
     * 由部署形态显式决定：
     * <ul>
     *     <li>{@code true}（多实例）：本地未命中目标用户时，通过 Redis Pub/Sub 广播到其他实例完成跨实例投递；</li>
     *     <li>{@code false}（单实例）：本地未命中即视为用户不在线，不发起广播，也不必处理自发自收。</li>
     * </ul>
     * 默认取 {@code true} 以保证 fail-safe——"忘配"时最坏只是多发一次无害的广播，
     * 而非多实例部署下跨实例用户收不到通知。
     */
    private boolean clusterMode = true;

    /**
     * @return SSE 连接超时（毫秒）
     */
    public long getSseTimeout() {
        return sseTimeout;
    }

    /**
     * @param sseTimeout SSE 连接超时（毫秒）
     */
    public void setSseTimeout(long sseTimeout) {
        this.sseTimeout = sseTimeout;
    }

    /**
     * @return SSE 心跳间隔（毫秒）
     */
    public long getSseHeartbeatInterval() {
        return sseHeartbeatInterval;
    }

    /**
     * @param sseHeartbeatInterval SSE 心跳间隔（毫秒）
     */
    public void setSseHeartbeatInterval(long sseHeartbeatInterval) {
        this.sseHeartbeatInterval = sseHeartbeatInterval;
    }

    /**
     * @return notify-center 专用 Redis 连接配置
     */
    public RedisProperties getRedis() {
        return redis;
    }

    /**
     * @param redis notify-center 专用 Redis 连接配置
     */
    public void setRedis(RedisProperties redis) {
        this.redis = redis;
    }

    /**
     * @return 是否多机集群模式
     */
    public boolean isClusterMode() {
        return clusterMode;
    }

    /**
     * @param clusterMode 是否多机集群模式
     */
    public void setClusterMode(boolean clusterMode) {
        this.clusterMode = clusterMode;
    }

    /**
     * notify-center 专用 Redis 连接配置，直接继承 Spring Boot 的 {@code RedisProperties}，
     * 保留向后扩展自定义字段的余地。
     */
    public static class RedisProperties extends org.springframework.boot.autoconfigure.data.redis.RedisProperties {
    }

}
