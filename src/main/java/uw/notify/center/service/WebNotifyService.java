package uw.notify.center.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import uw.common.dto.ResponseData;
import uw.common.util.JsonUtils;
import uw.notify.center.conf.UwNotifyCenterProperties;
import uw.notify.center.constant.Constants;
import uw.notify.client.vo.WebNotifyMsg;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 通知服务。
 */
@Component
public class WebNotifyService {

    private static final Logger log = LoggerFactory.getLogger( WebNotifyService.class );

    private static final ConcurrentHashMap<Long, SseEmitter> sseEmitterMap = new ConcurrentHashMap<>();

    private static UwNotifyCenterProperties uwNotifyCenterProperties;

    private static RedisTemplate<Long, Long> notifyRedisTemplate;

    public WebNotifyService(UwNotifyCenterProperties uwNotifyCenterProperties, RedisTemplate<Long, Long> notifyRedisTemplate) {
        WebNotifyService.uwNotifyCenterProperties = uwNotifyCenterProperties;
        WebNotifyService.notifyRedisTemplate = notifyRedisTemplate;
    }

    /**
     * 给指定用户发信息
     */
    public static ResponseData pushMsg(WebNotifyMsg webNotifyMsg, boolean autoRelay) {
        SseEmitter se = sseEmitterMap.get( webNotifyMsg.getUserId() );
        if (se == null && autoRelay) {
            //如果本地不在线，并且开启了自动转发，则在redis中广播发送。
            notifyRedisTemplate.convertAndSend( Constants.REDIS_NOTIFY_CHANNEL, JsonUtils.toString( webNotifyMsg ) );
            return ResponseData.WARN;
        }
        if (se == null) {
            return ResponseData.errorMsg( "指定用户[" + webNotifyMsg.getUserId() + "]不在线！" );
        }
        try {
            se.send( webNotifyMsg.getNotifyBody(), MediaType.APPLICATION_JSON );
            se.complete();
            return ResponseData.SUCCESS;
        } catch (Exception e) {
            log.error( "指定用户[{}]发送信息出错：{}", webNotifyMsg.getUserId(), e.getMessage(), e );
            return ResponseData.errorMsg( e.getMessage() );
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
    public static SseEmitter openStream(long userId) {
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