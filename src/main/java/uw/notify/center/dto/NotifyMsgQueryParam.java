package uw.notify.center.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import uw.dao.PageQueryParam;
import uw.dao.annotation.QueryMeta;

import java.util.Date;

/**
* 消息推送web端记录列表查询参数。
*/
@Schema(title = "消息推送web端记录列表查询参数", description = "消息推送web端记录列表查询参数")
public class NotifyMsgQueryParam extends PageQueryParam{

    /**
    * 主键ID
    */
    @QueryMeta(expr = "id=?")
    @Schema(title="主键ID", description = "主键ID")
    private Long id;

    /**
    * 用户ID
    */
    @QueryMeta(expr = "user_id=?")
    @Schema(title="用户ID", description = "用户ID")
    private Long userId;

    /**
    * 运营商编号
    */
    @QueryMeta(expr = "saas_id=?")
    @Schema(title="运营商编号", description = "运营商编号")
    private Long saasId;

    /**
    * 商户编号
    */
    @QueryMeta(expr = "mch_id=?")
    @Schema(title="商户编号", description = "商户编号")
    private Long mchId;

    /**
    * 用户类型
    */
    @QueryMeta(expr = "user_type=?")
    @Schema(title="用户类型", description = "用户类型")
    private Integer userType;

    /**
    * 通知类型
    */
    @QueryMeta(expr = "object_type like ?")
    @Schema(title="通知类型", description = "通知类型")
    private String objectType;

    /**
    * 业务关联ID
    */
    @QueryMeta(expr = "object_id=?")
    @Schema(title="业务关联ID", description = "业务关联ID")
    private Long objectId;

    /**
    * 消息内容
    */
    @QueryMeta(expr = "notify_subject like ?")
    @Schema(title="消息内容", description = "消息内容")
    private String notifySubject;

    /**
    * 跳转链接
    */
    @QueryMeta(expr = "action_url like ?")
    @Schema(title="跳转链接", description = "跳转链接")
    private String actionUrl;
    /**
    * 创建时间范围
    */
    @QueryMeta(expr = "create_date between ? and ?")
    @Schema(title="创建时间范围", description = "创建时间范围")
    private Date[] createDateRange;

    /**
    * 发送时间范围
    */
    @QueryMeta(expr = "sent_date between ? and ?")
    @Schema(title="发送时间范围", description = "发送时间范围")
    private Date[] sentDateRange;

    /**
    * 消息状态(0-待发送  1-已发送)
    */
    @QueryMeta(expr = "state=?")
    @Schema(title="消息状态(0-待发送  1-已发送)", description = "消息状态(0-待发送  1-已发送)")
    private Integer state;

    /**
    * 正常消息状态(0-待发送  1-已发送)
    */
    @QueryMeta(expr = "state>-1")
    @Schema(title="正常消息状态(0-待发送  1-已发送)", description = "正常消息状态(0-待发送  1-已发送)")
    private Boolean stateOn;

    /**
    * 消息状态(0-待发送  1-已发送)数组
    */
    @QueryMeta(expr = "state in (?)")
    @Schema(title="消息状态(0-待发送  1-已发送)数组", description = "消息状态(0-待发送  1-已发送)数组，可同时匹配多个状态。")
    private Integer[] states;

    /**
    * 消息状态(0-待发送  1-已发送)运算条件。
    * 可以使用运算符号。
    */
    @QueryMeta(expr = "state ?")
    @Schema(title="消息状态(0-待发送  1-已发送)运算条件", description = "消息状态(0-待发送  1-已发送)运算条件，可使用><=!比较运算符。")
    private String stateOp;



    /**
    * 获得主键ID。
    */
    public Long getId(){
        return this.id;
    }

    /**
    * 设置主键ID。
    */
    public void setId(Long id){
        this.id = id;
    }

    /**
    * 获得用户ID。
    */
    public Long getUserId(){
        return this.userId;
    }

    /**
    * 设置用户ID。
    */
    public void setUserId(Long userId){
        this.userId = userId;
    }

    /**
    * 获得运营商编号。
    */
    public Long getSaasId(){
        return this.saasId;
    }

    /**
    * 设置运营商编号。
    */
    public void setSaasId(Long saasId){
        this.saasId = saasId;
    }

    /**
    * 获得商户编号。
    */
    public Long getMchId(){
        return this.mchId;
    }

    /**
    * 设置商户编号。
    */
    public void setMchId(Long mchId){
        this.mchId = mchId;
    }

    /**
    * 获得用户类型。
    */
    public Integer getUserType(){
        return this.userType;
    }

    /**
    * 设置用户类型。
    */
    public void setUserType(Integer userType){
        this.userType = userType;
    }


    /**
    * 获得通知类型。
    */
    public String getObjectType(){
        return this.objectType;
    }

    /**
    * 设置通知类型。
    */
    public void setObjectType(String objectType){
        this.objectType = objectType;
    }

    /**
    * 获得业务关联ID。
    */
    public Long getObjectId(){
        return this.objectId;
    }

    /**
    * 设置业务关联ID。
    */
    public void setObjectId(Long objectId){
        this.objectId = objectId;
    }


    /**
    * 获得消息内容。
    */
    public String getNotifySubject(){
        return this.notifySubject;
    }

    /**
    * 设置消息内容。
    */
    public void setNotifySubject(String notifySubject){
        this.notifySubject = notifySubject;
    }



    /**
    * 获得跳转链接。
    */
    public String getActionUrl(){
        return this.actionUrl;
    }

    /**
    * 设置跳转链接。
    */
    public void setActionUrl(String actionUrl){
        this.actionUrl = actionUrl;
    }


    /**
    * 获得创建时间范围。
    */
    public Date[] getCreateDateRange(){
        return this.createDateRange;
    }

    /**
    * 设置创建时间范围。
    */
    public void setCreateDateRange(Date[] createDateRange){
        this.createDateRange = createDateRange;
    }


    /**
    * 获得发送时间范围。
    */
    public Date[] getSentDateRange(){
        return this.sentDateRange;
    }

    /**
    * 设置发送时间范围。
    */
    public void setSentDateRange(Date[] sentDateRange){
        this.sentDateRange = sentDateRange;
    }

    /**
    * 获得消息状态(0-待发送  1-已发送)。
    */
    public Integer getState(){
        return this.state;
    }

    /**
    * 设置消息状态(0-待发送  1-已发送)。
    */
    public void setState(Integer state){
        this.state = state;
    }

    /**
    * 获得正常消息状态(0-待发送  1-已发送)。
    */
    public Boolean getStateOn(){
        return this.stateOn;
    }

    /**
    * 设置正常消息状态(0-待发送  1-已发送)。
    */
    public void setStateOn(Boolean stateOn){
        this.stateOn = stateOn;
    }

    /**
    * 获得消息状态(0-待发送  1-已发送)数组。
    */
    public Integer[] getStates(){
        return this.states;
    }

    /**
    * 设置消息状态(0-待发送  1-已发送)数组。
    */
    public void setStates(Integer[] states){
        this.states = states;
    }

    /**
    * 获得消息状态(0-待发送  1-已发送)运算条件。
    */
    public String getStateOp(){
        return this.stateOp;
    }

    /**
    * 设置消息状态(0-待发送  1-已发送)运算条件。
    */
    public void setStateOp(String stateOp){
        this.stateOp = stateOp;
    }

}