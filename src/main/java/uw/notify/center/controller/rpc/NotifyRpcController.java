package uw.notify.center.controller.rpc;


import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uw.auth.service.annotation.MscPermDeclare;
import uw.auth.service.annotation.ResponseAdviceIgnore;
import uw.auth.service.constant.UserType;
import uw.common.dto.ResponseData;
import uw.notify.center.service.WebNotifyService;
import uw.notify.client.vo.WebNotifyMsg;

/**
 * rpc控制器。
 */
@RestController
@RequestMapping("/rpc/notify")
@ResponseAdviceIgnore
public class NotifyRpcController {

    /**
     * 推送通知。
     */
    @PostMapping("/pushNotify")
    @MscPermDeclare(user = UserType.RPC)
    public ResponseData pushNotify(@RequestBody WebNotifyMsg webNotifyMsg) {
        return WebNotifyService.pushMsg( webNotifyMsg,true );
    }

}
