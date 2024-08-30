package uw.notify.center.controller.user;


import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import uw.auth.service.AuthServiceHelper;
import uw.auth.service.annotation.MscPermDeclare;
import uw.auth.service.annotation.ResponseAdviceIgnore;
import uw.auth.service.constant.AuthType;
import uw.notify.center.service.WebNotifyService;

/**
 * 建立SSE连接。
 */
@RestController
@RequestMapping("/user/notify")
@ResponseAdviceIgnore
public class NotifyUserController {

    @GetMapping(value = "/stream", produces = {MediaType.TEXT_EVENT_STREAM_VALUE})
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
