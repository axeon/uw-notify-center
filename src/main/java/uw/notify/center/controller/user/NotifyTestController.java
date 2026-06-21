package uw.notify.center.controller.user;


import io.swagger.v3.oas.annotations.Operation;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uw.auth.service.AuthServiceHelper;
import uw.auth.service.annotation.MscPermDeclare;
import uw.auth.service.constant.AuthType;
import uw.common.response.ResponseData;
import uw.notify.client.NotifyClientHelper;
import uw.notify.client.vo.WebNotifyMsg;

/**
 * 通知测试接口，仅在 {@code debug}/{@code dev} profile 下生效。
 * <p>
 * 用于联调验证：以当前登录用户身份自发自收一条通知，验证从客户端到 SSE 投递的完整链路。
 *
 * @author axeon
 */
@Profile({"debug","dev"})
@RestController
@RequestMapping("/user/test")
public class NotifyTestController {

    /**
     * 以当前登录用户为目标，自发自收一条通知。
     * <p>
     * 入参的 {@code userId}/{@code saasId} 会被当前登录用户身份覆盖，无需调用方传入。
     *
     * @param webNotifyMsg 通知消息体（仅需填充 {@code notifyBody}）
     * @return 推送结果
     */
    @PostMapping("/send")
    @Operation(summary = "自发自收", description = "自发自收")
    @MscPermDeclare(auth = AuthType.NONE)
    public ResponseData<Void> send(@RequestBody WebNotifyMsg webNotifyMsg) {
        webNotifyMsg.setUserId( AuthServiceHelper.getUserId() );
        webNotifyMsg.setSaasId( AuthServiceHelper.getSaasId() );
        return NotifyClientHelper.pushNotify( webNotifyMsg );
    }

}
