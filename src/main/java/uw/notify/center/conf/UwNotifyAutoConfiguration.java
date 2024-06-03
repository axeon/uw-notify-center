package uw.notify.center.conf;

import io.lettuce.core.resource.ClientResources;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import uw.notify.center.constant.Constants;
import uw.notify.center.listener.RedisNotifyListener;

/**
 * 启动配置。
 */
@Configuration
@EnableConfigurationProperties({UwNotifyCenterProperties.class})
@AutoConfigureAfter({RedisAutoConfiguration.class})
public class UwNotifyAutoConfiguration {
    private static final Logger log = LoggerFactory.getLogger( UwNotifyAutoConfiguration.class );

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisTemplate<String, byte[]> fusionCacheRedisTemplate) {
        RedisMessageListenerContainer redisMessageListenerContainer = new RedisMessageListenerContainer();
        redisMessageListenerContainer.setConnectionFactory( fusionCacheRedisTemplate.getConnectionFactory() );
        RedisNotifyListener notifyListener = new RedisNotifyListener();
        redisMessageListenerContainer.addMessageListener( notifyListener, new ChannelTopic( Constants.REDIS_NOTIFY_CHANNEL ) );
        return redisMessageListenerContainer;
    }

    /**
     * 用户定位服务缓存。
     *
     * @param uwNotifyCenterProperties
     * @param clientResources
     * @return
     */
    @Bean
    public RedisTemplate<Long, Long> notifyRedisTemplate(final UwNotifyCenterProperties uwNotifyCenterProperties,
                                                         final ClientResources clientResources) {
        RedisTemplate<Long, Long> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer( new GenericToStringSerializer<Long>( Long.class ) );
        redisTemplate.setValueSerializer( new GenericToStringSerializer<Long>( Long.class ) );
        redisTemplate.setConnectionFactory( redisConnectionFactory( uwNotifyCenterProperties.getRedis(), clientResources ) );
        redisTemplate.setEnableDefaultSerializer( false );
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    /**
     * Redis连接工厂
     *
     * @param redisProperties
     * @param clientResources
     * @return
     */
    private RedisConnectionFactory redisConnectionFactory(RedisProperties redisProperties,
                                                          ClientResources clientResources) {
        RedisProperties.Pool pool = redisProperties.getLettuce().getPool();
        LettuceClientConfiguration.LettuceClientConfigurationBuilder builder;
        if (pool == null) {
            builder = LettuceClientConfiguration.builder();
        } else {
            GenericObjectPoolConfig config = new GenericObjectPoolConfig();
            config.setMaxTotal( pool.getMaxActive() );
            config.setMaxIdle( pool.getMaxIdle() );
            config.setMinIdle( pool.getMinIdle() );
            if (pool.getMaxWait() != null) {
                config.setMaxWait( pool.getMaxWait() );
            }
            builder = LettucePoolingClientConfiguration.builder().poolConfig( config );
        }

        if (redisProperties.getTimeout() != null) {
            builder.commandTimeout( redisProperties.getTimeout() );
        }
        if (redisProperties.getLettuce() != null) {
            RedisProperties.Lettuce lettuce = redisProperties.getLettuce();
            if (lettuce.getShutdownTimeout() != null && !lettuce.getShutdownTimeout().isZero()) {
                builder.shutdownTimeout( redisProperties.getLettuce().getShutdownTimeout() );
            }
        }
        builder.clientResources( clientResources );
        LettuceClientConfiguration config = builder.build();

        RedisStandaloneConfiguration standaloneConfig = new RedisStandaloneConfiguration();
        standaloneConfig.setHostName( redisProperties.getHost() );
        standaloneConfig.setPort( redisProperties.getPort() );
        standaloneConfig.setPassword( RedisPassword.of( redisProperties.getPassword() ) );
        standaloneConfig.setDatabase( redisProperties.getDatabase() );

        LettuceConnectionFactory factory = new LettuceConnectionFactory( standaloneConfig, config );
        factory.afterPropertiesSet();
        return factory;
    }

}
