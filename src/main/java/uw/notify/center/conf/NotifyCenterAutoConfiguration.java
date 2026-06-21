package uw.notify.center.conf;

import io.lettuce.core.resource.ClientResources;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
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
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.support.TaskExecutorAdapter;
import uw.notify.center.constant.Constants;
import uw.notify.center.listener.RedisNotifyListener;

import java.util.concurrent.Executors;

/**
 * notify-center 启动配置。
 * <p>
 * 负责装配通知中心运行所需的核心基础设施：
 * <ul>
 *     <li>notify-center 自用的 {@link RedisTemplate}（内部持有 Lettuce 连接工厂，发布与订阅共享）；</li>
 *     <li>{@link RedisMessageListenerContainer}，订阅跨实例广播通道并把消息派发给 {@link RedisNotifyListener}。</li>
 * </ul>
 * 本项目 Redis 仅服务 notify 一个用途，连接工厂无需独立暴露，由 {@link RedisTemplate} 内部持有即可，
 * 监听容器复用 {@link RedisTemplate#getConnectionFactory()} 拿到同一实例。
 *
 * @author axeon
 */
@Configuration
@EnableConfigurationProperties({UwNotifyCenterProperties.class})
@AutoConfigureAfter({RedisAutoConfiguration.class})
public class NotifyCenterAutoConfiguration {

    /**
     * Redis Pub/Sub 订阅容器。
     * <p>
     * 订阅 {@link Constants#REDIS_NOTIFY_CHANNEL} 通道，由 {@link RedisNotifyListener} 解析并投递消息。
     * 复用 {@code notifyRedisTemplate} 的连接工厂，避免单独建连接池。
     * 任务派发与订阅连接均使用带命名前缀（{@code uw-notify-}）的虚拟线程池：消息处理是大量短任务，
     * 虚拟线程几乎无上限，既避免默认 {@code SimpleAsyncTaskExecutor} 无限创建平台线程，
     * 也不会像有界线程池那样在突发流量下堆积丢消息。
     *
     * @param notifyRedisTemplate notify-center 自用的 Redis 模板（提供共享连接工厂）
     * @return 已完成配置的 Redis 消息监听容器
     */
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisTemplate<String, String> notifyRedisTemplate) {
        RedisMessageListenerContainer redisMessageListenerContainer = new RedisMessageListenerContainer();
        redisMessageListenerContainer.setConnectionFactory( notifyRedisTemplate.getConnectionFactory() );
        // 消息派发与订阅均使用带命名的虚拟线程池：消息处理是大量短任务，虚拟线程几乎无上限，
        // 既避免默认 SimpleAsyncTaskExecutor 无限建平台线程，也不会像有界线程池那样在高 TPS 下堆积丢消息；
        // 命名前缀便于在线程 dump / 日志中定位订阅消费线程。
        AsyncTaskExecutor taskExecutor = new TaskExecutorAdapter(
                Executors.newThreadPerTaskExecutor( Thread.ofVirtual().name( "uw-notify-", 0 ).factory() ) );
        redisMessageListenerContainer.setTaskExecutor( taskExecutor );
        redisMessageListenerContainer.setSubscriptionExecutor( taskExecutor );
        redisMessageListenerContainer.addMessageListener( new RedisNotifyListener(), new ChannelTopic( Constants.REDIS_NOTIFY_CHANNEL ) );
        return redisMessageListenerContainer;
    }

    /**
     * notify-center 自用的 {@link RedisTemplate}。
     * <p>
     * 全部 key/value 采用 {@link StringRedisSerializer}，内部持有基于 {@code uw.notify.center.redis} 配置构建的
     * Lettuce 连接工厂，用于向跨实例广播通道 {@link Constants#REDIS_NOTIFY_CHANNEL} 发布 {@code WebNotifyMsg} 的 JSON 文本，
     * 并由 {@link #redisMessageListenerContainer} 复用其连接工厂完成订阅。
     *
     * @param uwNotifyCenterProperties notify-center 配置（含 Redis 连接参数）
     * @param clientResources          共享的 Lettuce {@link ClientResources}
     * @return 已完成序列化配置的 String RedisTemplate
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
     * 基于 standalone 模式构建 Lettuce {@link RedisConnectionFactory}。
     * <p>
     * 读取连接池、命令超时、shutdown 超时、SSL、用户名/密码等配置项（缺省时回退 Spring Boot 默认值），
     * 供 {@link RedisTemplate} 内部持有，支撑 Pub/Sub 发布与订阅。
     *
     * @param redisProperties Redis 连接配置（前缀 {@code uw.notify.center.redis}）
     * @param clientResources 共享的 Lettuce {@link ClientResources}
     * @return 已完成 {@code afterPropertiesSet} 的 {@link LettuceConnectionFactory}
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
