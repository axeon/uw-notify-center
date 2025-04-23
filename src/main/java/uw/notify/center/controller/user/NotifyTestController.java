package uw.notify.center.controller.user;


import io.swagger.v3.oas.annotations.Operation;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uw.auth.service.AuthServiceHelper;
import uw.auth.service.annotation.MscPermDeclare;
import uw.auth.service.annotation.ResponseAdviceIgnore;
import uw.auth.service.constant.AuthType;
import uw.common.dto.ResponseData;
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
    @Operation(summary = "自发自收", description = "自发自收")
    @MscPermDeclare(auth = AuthType.NONE)
    public ResponseData send(@RequestBody WebNotifyMsg webNotifyMsg) {
        webNotifyMsg.setUserId( AuthServiceHelper.getUserId() );
        webNotifyMsg.setSaasId( AuthServiceHelper.getSaasId() );
        return NotifyClientHelper.pushNotify( webNotifyMsg );
    }

}
