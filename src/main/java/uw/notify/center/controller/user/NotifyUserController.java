package uw.notify.center.controller.user;


import io.swagger.v3.oas.annotations.Operation;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import uw.auth.service.AuthServiceHelper;
import uw.auth.service.annotation.MscPermDeclare;
import uw.auth.service.annotation.ResponseAdviceIgnore;
import uw.auth.service.constant.AuthType;
import uw.notify.center.service.WebNotifyService;

/**
 * 对外提供短链转发功能。
 * 一般需要在nginx上设置重写。
 */
@Controller
@RequestMapping("/user/notify")
@ResponseAdviceIgnore
public class NotifyUserController {

    @GetMapping("/stream")
    @Operation(summary = "建立连接", description = "建立连接")
    @MscPermDeclare(auth = AuthType.NONE)
    public SseEmitter stream() {
        long userId = AuthServiceHelper.getUserId();
        if (userId > 0) {
            return WebNotifyService.openStream( userId );
        } else {
            return null;
        }
    }

}
