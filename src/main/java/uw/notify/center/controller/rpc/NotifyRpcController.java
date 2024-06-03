package uw.notify.center.controller.rpc;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uw.auth.service.annotation.MscPermDeclare;
import uw.auth.service.constant.ActionLog;
import uw.auth.service.constant.UserType;
import uw.common.dto.ResponseData;
import uw.notify.center.service.NotifyCenterService;
import uw.notify.center.vo.NotifyMsgVo;

/**
 * rpc控制器。
 */
@RestController
@RequestMapping("/rpc/notify")
public class NotifyRpcController {


    private NotifyCenterService notifyService;

    @Autowired
    public NotifyRpcController(NotifyCenterService notifyService) {
        this.notifyService = notifyService;
    }

    /**
     * 推送通知。
     */
    @PostMapping("/pushNotify")
    @MscPermDeclare(type = UserType.RPC, log = ActionLog.NONE)
    public ResponseData pushNotify(@RequestBody NotifyMsgVo notifyMsgVo) {
        NotifyCenterService.pushMsg( notifyMsgVo );
        return null;
    }

}
