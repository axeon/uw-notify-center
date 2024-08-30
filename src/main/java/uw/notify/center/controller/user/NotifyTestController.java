package uw.notify.center.controller.user;


import io.swagger.v3.oas.annotations.Operation;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import uw.auth.service.AuthServiceHelper;
import uw.auth.service.annotation.MscPermDeclare;
import uw.auth.service.annotation.ResponseAdviceIgnore;
import uw.auth.service.constant.AuthType;
import uw.common.dto.ResponseData;
import uw.notify.client.NotifyClientHelper;
import uw.notify.client.vo.WebNotifyMsg;

/**
 * 对外提供短链转发功能。
 * 一般需要在nginx上设置重写。
 */
@Profile({"default", "test", "dev"})
@Controller
@RequestMapping("/user/test")
@ResponseAdviceIgnore
public class NotifyTestController {

    @GetMapping("/send")
    @Operation(summary = "发送消息", description = "发送消息")
    @MscPermDeclare(auth = AuthType.NONE)
    public ResponseData send(WebNotifyMsg webNotifyMsg) {
        webNotifyMsg.setUserId( AuthServiceHelper.getUserId() );
        webNotifyMsg.setSaasId( AuthServiceHelper.getSaasId() );
        webNotifyMsg.setMchId( AuthServiceHelper.getMchId() );
        webNotifyMsg.setUserType( AuthServiceHelper.getUserType() );
        return NotifyClientHelper.pushNotify( webNotifyMsg );
    }

}
