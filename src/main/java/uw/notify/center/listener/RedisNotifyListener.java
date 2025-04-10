package uw.notify.center.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import uw.common.util.JsonUtils;
import uw.notify.center.service.WebNotifyService;
import uw.notify.client.vo.WebNotifyMsg;


/**
 * Redis通知的监听器。
 * 一般用于多个实例之间传递消息。
 */
public class RedisNotifyListener implements MessageListener {

    private final Logger logger = LoggerFactory.getLogger( RedisNotifyListener.class );

    @Override
    public void onMessage(Message message, byte[] pattern) {
        WebNotifyMsg webNotifyMsg = JsonUtils.parse( message.getBody(), WebNotifyMsg.class );
        if (webNotifyMsg != null) {
            WebNotifyService.pushMsg( webNotifyMsg, false );
        }
    }
}
