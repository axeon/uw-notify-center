package uw.notify.center.conf;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 通知中心配置类。
 *
 * @author axeon
 */
@Configuration
@ConfigurationProperties(prefix = "uw.notify.center")
public class UwNotifyCenterProperties {

    /**
     * sse超时时间。
     */
    private long sseTimeout = 60_000L;

    /**
     * Redis配置
     */
    private RedisProperties redis = new RedisProperties();

    public long getSseTimeout() {
        return sseTimeout;
    }

    public void setSseTimeout(long sseTimeout) {
        this.sseTimeout = sseTimeout;
    }

    public RedisProperties getRedis() {
        return redis;
    }

    public void setRedis(RedisProperties redis) {
        this.redis = redis;
    }

    public static class RedisProperties extends org.springframework.boot.autoconfigure.data.redis.RedisProperties {
    }

}
