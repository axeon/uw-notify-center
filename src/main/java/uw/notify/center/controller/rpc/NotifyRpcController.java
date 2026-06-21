package uw.notify.center.controller.rpc;


import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uw.auth.service.annotation.MscPermDeclare;
import uw.auth.service.constant.UserType;
import uw.common.response.ResponseData;
import uw.notify.center.service.WebNotifyService;
import uw.notify.client.vo.WebNotifyMsg;

/**
 * 服务间 RPC 通知接口。
 * <p>
 * 供 {@code uw-notify-client}（{@code NotifyClientHelper}）经 HTTP RPC 调用，
 * 由本服务将通知投递给目标在线用户，必要时通过 Redis Pub/Sub 转发到目标实例。
 *
 * @author axeon
 */
@RestController
@RequestMapping("/rpc/notify")
public class NotifyRpcController {

    /**
     * 推送 Web 通知。
     * <p>
     * 以 {@code autoRelay=true} 调用 {@link WebNotifyService#pushMsg}：当目标用户不在当前实例时，
     * 自动通过 Redis 广播到全部实例，由持有连接的实例完成投递。
     *
     * @param webNotifyMsg 通知消息体（{@code userId} 必须 &gt; 0）
     * @return 投递结果
     */
    @PostMapping("/pushNotify")
    @MscPermDeclare(user = UserType.RPC)
    public ResponseData pushNotify(@RequestBody WebNotifyMsg webNotifyMsg) {
        return WebNotifyService.pushMsg( webNotifyMsg,true );
    }

}
