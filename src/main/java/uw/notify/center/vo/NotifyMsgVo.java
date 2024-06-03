package uw.notify.center.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

/**
 * NotifyMsgVo类。
 *
 * @author axeon
 */
@Schema(title = "消息推送web端记录", description = "消息推送web端记录")
public class NotifyMsgVo implements Serializable{

	/**
	 * 用户ID
	 */
	@Schema(title = "用户ID", description = "用户ID")
	private long userId;

	/**
	 * 运营商编号
	 */
	@Schema(title = "运营商编号", description = "运营商编号")
	private long saasId;

	/**
	 * 商户编号
	 */
	@Schema(title = "商户编号", description = "商户编号")
	private long mchId;

	/**
	 * 用户类型
	 */
	@Schema(title = "用户类型", description = "用户类型")
	private int userType;

	/**
	 * 通知类型
	 */
	@Schema(title = "通知类型", description = "通知类型")
	private String objectType;

	/**
	 * 业务关联ID
	 */
	@Schema(title = "业务关联ID", description = "业务关联ID")
	private long objectId;

	/**
	 * 消息内容
	 */
	@Schema(title = "消息内容", description = "消息内容")
	private String notifySubject;

	/**
	 * 消息内容
	 */
	@Schema(title = "消息内容", description = "消息内容")
	private String notifyContent;

	/**
	 * 跳转链接
	 */
	@Schema(title = "跳转链接", description = "跳转链接")
	private String actionUrl;

	/**
	 * 通知类型。
	 */
	@Schema(title = "通知类型(0-即时发送  1-持久化)", description = "通知类型(0-即时发送  1-持久化)")
	private int notifyType;

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public long getSaasId() {
		return saasId;
	}

	public void setSaasId(long saasId) {
		this.saasId = saasId;
	}

	public long getMchId() {
		return mchId;
	}

	public void setMchId(long mchId) {
		this.mchId = mchId;
	}

	public int getUserType() {
		return userType;
	}

	public void setUserType(int userType) {
		this.userType = userType;
	}

	public String getObjectType() {
		return objectType;
	}

	public void setObjectType(String objectType) {
		this.objectType = objectType;
	}

	public long getObjectId() {
		return objectId;
	}

	public void setObjectId(long objectId) {
		this.objectId = objectId;
	}

	public String getNotifySubject() {
		return notifySubject;
	}

	public void setNotifySubject(String notifySubject) {
		this.notifySubject = notifySubject;
	}

	public String getNotifyContent() {
		return notifyContent;
	}

	public void setNotifyContent(String notifyContent) {
		this.notifyContent = notifyContent;
	}

	public String getActionUrl() {
		return actionUrl;
	}

	public void setActionUrl(String actionUrl) {
		this.actionUrl = actionUrl;
	}

	public int getNotifyType() {
		return notifyType;
	}

	public void setNotifyType(int notifyType) {
		this.notifyType = notifyType;
	}
}