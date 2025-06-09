package uw.notify.center.conf;

import io.lettuce.core.resource.ClientResources;
import org.apache.commons.lang3.StringUtils;
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
public class NotifyCenterAutoConfiguration {
    private static final Logger log = LoggerFactory.getLogger( NotifyCenterAutoConfiguration.class );

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisTemplate<Long, Long> notifyRedisTemplate) {
        RedisMessageListenerContainer redisMessageListenerContainer = new RedisMessageListenerContainer();
        redisMessageListenerContainer.setConnectionFactory( notifyRedisTemplate.getConnectionFactory() );
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
    public RedisTemplate<Long, Long> notifyRedisTemplate(final UwNotifyCenterProperties uwNotifyCenterProperties, final ClientResources clientResources) {
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
    private RedisConnectionFactory redisConnectionFactory(RedisProperties redisProperties, ClientResources clientResources) {
        //设置连接池。
        RedisProperties.Pool poolProperties = redisProperties.getLettuce().getPool();
        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        poolConfig.setMaxTotal( poolProperties.getMaxActive() );
        poolConfig.setMaxIdle( poolProperties.getMaxIdle() );
        poolConfig.setMinIdle( poolProperties.getMinIdle() );
        if (poolProperties.getMaxWait() != null) {
            poolConfig.setMaxWait( poolProperties.getMaxWait() );
        }
        LettucePoolingClientConfiguration.LettucePoolingClientConfigurationBuilder builder = LettucePoolingClientConfiguration.builder().poolConfig( poolConfig );
        if (redisProperties.getTimeout() != null) {
            builder.commandTimeout( redisProperties.getTimeout() );
        }
        //设置shutdownTimeout。
        RedisProperties.Lettuce lettuce = redisProperties.getLettuce();
        if (lettuce.getShutdownTimeout() != null && !lettuce.getShutdownTimeout().isZero()) {
            builder.shutdownTimeout( redisProperties.getLettuce().getShutdownTimeout() );
        }
        //设置clientResources。
        builder.clientResources( clientResources );
        //设置ssl。
        if (redisProperties.getSsl().isEnabled()) {
            builder.useSsl();
        }
        //构建standaloneConfig。
        LettuceClientConfiguration clientConfig = builder.build();
        RedisStandaloneConfiguration standaloneConfig = new RedisStandaloneConfiguration();
        standaloneConfig.setHostName( redisProperties.getHost() );
        standaloneConfig.setPort( redisProperties.getPort() );
        standaloneConfig.setDatabase( redisProperties.getDatabase() );
        if (StringUtils.isNotBlank(redisProperties.getUsername())) {
            standaloneConfig.setUsername( redisProperties.getUsername() );
        }
        if (StringUtils.isNotBlank(redisProperties.getPassword())) {
            standaloneConfig.setPassword( RedisPassword.of( redisProperties.getPassword() ) );
        }
        LettuceConnectionFactory factory = new LettuceConnectionFactory( standaloneConfig, clientConfig );
        factory.afterPropertiesSet();
        return factory;
    }

}
