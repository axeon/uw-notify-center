package uw.notify.center.controller.user;


import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import uw.auth.service.AuthServiceHelper;
import uw.auth.service.annotation.MscPermDeclare;
import uw.auth.service.annotation.ResponseAdviceIgnore;
import uw.auth.service.constant.AuthType;
import uw.notify.center.service.WebNotifyService;

/**
 * 面向前端用户的 SSE 通知端点。
 * <p>
 * 客户端建立长连接后，notify-center 通过该连接实时下发通知。
 * 标注 {@link ResponseAdviceIgnore} 以避免统一响应包装破坏 SSE 事件流。
 *
 * @author axeon
 */
@RestController
@RequestMapping("/user/notify")
@ResponseAdviceIgnore
public class NotifyUserController {

    /**
     * 建立当前登录用户的 SSE 通知连接。
     * <p>
     * 同一用户重复建连会自动关闭旧连接；连接超时与错误由 {@link WebNotifyService} 统一清理。
     *
     * @return 与当前用户绑定的 {@link SseEmitter}
     * @throws ResponseStatusException 当用户未认证（{@code userId<=0}）时返回 401
     */
    @GetMapping(value = "/stream", produces = {MediaType.TEXT_EVENT_STREAM_VALUE})
    @Operation(summary = "SSE端点", description = "SSE端点")
    @MscPermDeclare(auth = AuthType.NONE)
    public SseEmitter stream() {
        long userId = AuthServiceHelper.getUserId();
        if (userId > 0) {
            return WebNotifyService.openStream( userId );
        } else {
            throw new ResponseStatusException( HttpStatus.UNAUTHORIZED, "用户未认证" );
        }
    }

}
