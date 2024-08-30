package uw.notify.center.controller.user;


import io.swagger.v3.oas.annotations.Operation;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import uw.auth.service.AuthServiceHelper;
import uw.auth.service.annotation.MscPermDeclare;
import uw.auth.service.annotation.ResponseAdviceIgnore;
import uw.auth.service.constant.AuthType;
import uw.common.dto.ResponseData;
import uw.notify.center.service.WebNotifyService;
import uw.notify.client.NotifyClientHelper;
import uw.notify.client.vo.WebNotifyMsg;

/**
 * 测试接口。
 */
@Profile({"default", "test", "dev"})
@RestController
@RequestMapping("/user/test")
@ResponseAdviceIgnore
public class NotifyTestController {

    @PostMapping("/send")
    @Operation(summary = "给自己发送测试消息", description = "给自己发送测试消息")
    @MscPermDeclare(auth = AuthType.NONE)
    public ResponseData send(@RequestBody WebNotifyMsg webNotifyMsg) {
        webNotifyMsg.setUserId( AuthServiceHelper.getUserId() );
        webNotifyMsg.setSaasId( AuthServiceHelper.getSaasId() );
        webNotifyMsg.setMchId( AuthServiceHelper.getMchId() );
        webNotifyMsg.setUserType( AuthServiceHelper.getUserType() );
        return NotifyClientHelper.pushNotify( webNotifyMsg );
    }

}
