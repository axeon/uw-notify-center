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
import uw.notify.center.constant.Constants;
import org.springframework.data.redis.serializer.StringRedisSerializer;
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
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisTemplate<String, String> notifyRedisTemplate) {
        RedisMessageListenerContainer redisMessageListenerContainer = new RedisMessageListenerContainer();
        redisMessageListenerContainer.setConnectionFactory( notifyRedisTemplate.getConnectionFactory() );
        redisMessageListenerContainer.addMessageListener( new RedisNotifyListener(), new ChannelTopic( Constants.REDIS_NOTIFY_CHANNEL ) );
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
    public RedisTemplate<String, String> notifyRedisTemplate(final UwNotifyCenterProperties uwNotifyCenterProperties, final ClientResources clientResources) {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        redisTemplate.setKeySerializer( stringSerializer );
        redisTemplate.setValueSerializer( stringSerializer );
        redisTemplate.setHashKeySerializer( stringSerializer );
        redisTemplate.setHashValueSerializer( stringSerializer );
        redisTemplate.setConnectionFactory( redisConnectionFactory( uwNotifyCenterProperties.getRedis(), clientResources ) );
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
        RedisProperties.Lettuce lettuce = redisProperties.getLettuce();
        if (lettuce == null) {
            lettuce = new RedisProperties.Lettuce();
        }
        RedisProperties.Pool poolProperties = lettuce.getPool();
        if (poolProperties == null) {
            poolProperties = new RedisProperties.Pool();
        }
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
        if (lettuce.getShutdownTimeout() != null && !lettuce.getShutdownTimeout().isZero()) {
            builder.shutdownTimeout( lettuce.getShutdownTimeout() );
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
