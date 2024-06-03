package uw.notify.center.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import uw.notify.center.conf.UwNotifyCenterProperties;
import uw.notify.center.constant.Constants;
import uw.notify.center.vo.NotifyMsgVo;
import uw.notify.center.util.NotifyJsonUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 通知服务。
 */
@Component
public class NotifyCenterService {

    private static final Logger log = LoggerFactory.getLogger( NotifyCenterService.class );

    private static ConcurrentHashMap<Long, SseEmitter> sseEmitterMap = new ConcurrentHashMap<>();

    private static UwNotifyCenterProperties uwNotifyCenterProperties;

    private static RedisTemplate<Long, Long> notifyRedisTemplate;

    public NotifyCenterService(UwNotifyCenterProperties uwNotifyCenterProperties, RedisTemplate<Long, Long> notifyRedisTemplate) {
        NotifyCenterService.uwNotifyCenterProperties = uwNotifyCenterProperties;
        NotifyCenterService.notifyRedisTemplate = notifyRedisTemplate;
    }

    /**
     * 给指定用户发信息
     */
    public static void pushMsg(NotifyMsgVo notifyMsgVo) {
        SseEmitter se = sseEmitterMap.get( notifyMsgVo.getUserId() );
        if (se == null) {
            //在redis中广播发送。
            notifyRedisTemplate.convertAndSend( Constants.REDIS_NOTIFY_CHANNEL, NotifyJsonUtils.toJSONString( notifyMsgVo ) );
        }
        try {
            se.send( notifyMsgVo, MediaType.APPLICATION_JSON );
        } catch (Exception e) {
            log.error( "userId:{},发送信息出错:{}", notifyMsgVo.getUserId(), e.getMessage(), e );
        }
    }

    /**
     * 获得ID列表。
     *
     * @return
     */
    public static List<Long> getOnlineIds() {
        return new ArrayList<>( sseEmitterMap.keySet() );
    }

    /**
     * 获得在线数。
     *
     * @return
     */
    public static int getOnlineCount() {
        return sseEmitterMap.size();
    }

    /**
     * 构造timeout。
     *
     * @param userId
     * @return
     */
    public static SseEmitter connect(long userId) {
        // 设置超时日期，0表示不过期
        SseEmitter sseEmitter = new SseEmitter( uwNotifyCenterProperties.getSseTimeout() );
        // 注册回调
        sseEmitter.onCompletion( () -> removeUser( userId ) );
        sseEmitter.onError( throwable -> {
            removeUser( userId );
            log.error( "SSE错误:{}", throwable.getMessage(), throwable );
        } );
        sseEmitter.onTimeout( () -> removeUser( userId ) );
        sseEmitterMap.put( userId, sseEmitter );
        return sseEmitter;
    }

    /**
     * 移出用户
     */
    private static void removeUser(long userId) {
        sseEmitterMap.remove( userId );
        if (log.isTraceEnabled()) {
            log.trace( "remove user id:{}, remain online count:{}. ", userId, sseEmitterMap.size() );
        }
    }

}