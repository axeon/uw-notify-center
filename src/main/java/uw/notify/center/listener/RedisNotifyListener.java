package uw.notify.center.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import uw.common.util.JsonUtils;
import uw.notify.center.service.WebNotifyService;
import uw.notify.client.vo.WebNotifyMsg;


/**
 * Redis Pub/Sub 通知监听器。
 * <p>
 * 订阅 {@link Constants#REDIS_NOTIFY_CHANNEL} 通道，接收来自其他实例广播的 {@link WebNotifyMsg}，
 * 反序列化后以 {@code autoRelay=false} 调用 {@link WebNotifyService#pushMsg} 完成本地投递。
 * <p>
 * 关闭 autoRelay 是为了避免订阅回调再次触发 Redis 广播，形成循环放大。
 * 反序列化失败仅记录错误日志，不向上抛出，单条脏消息不影响后续消费。
 *
 * @author axeon
 */
public class RedisNotifyListener implements MessageListener {

    private final Logger logger = LoggerFactory.getLogger( RedisNotifyListener.class );

    /**
     * 处理收到的 Redis 订阅消息。
     *
     * @param message 消息体（{@link WebNotifyMsg} 的 JSON 字节流）
     * @param pattern 订阅的通道模式（未使用）
     */
    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            WebNotifyMsg webNotifyMsg = JsonUtils.parse( message.getBody(), WebNotifyMsg.class );
            if (webNotifyMsg != null) {
                WebNotifyService.pushMsg( webNotifyMsg, false );
            }
        } catch (Exception e) {
            logger.error( "解析Redis通知消息失败: {}", e.getMessage(), e );
        }
    }
}
