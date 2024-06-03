package uw.notify.center.entity;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import io.swagger.v3.oas.annotations.media.Schema;
import uw.dao.DataEntity;
import uw.dao.annotation.ColumnMeta;
import uw.dao.annotation.TableMeta;

/**
 * NotifyMsg实体类
 * 消息推送web端记录
 *
 * @author axeon
 */
@TableMeta(tableName="notify_msg",tableType="table")
@Schema(title = "消息推送web端记录", description = "消息推送web端记录")
public class NotifyMsg implements DataEntity,Serializable{


	/**
	 * 主键ID
	 */
	@ColumnMeta(columnName="id", dataType="long", dataSize=19, nullable=false, primaryKey=true)
	@Schema(title = "主键ID", description = "主键ID")
	private long id;

	/**
	 * 用户ID
	 */
	@ColumnMeta(columnName="user_id", dataType="long", dataSize=19, nullable=false)
	@Schema(title = "用户ID", description = "用户ID")
	private long userId;

	/**
	 * 运营商编号
	 */
	@ColumnMeta(columnName="saas_id", dataType="long", dataSize=19, nullable=false)
	@Schema(title = "运营商编号", description = "运营商编号")
	private long saasId;

	/**
	 * 商户编号
	 */
	@ColumnMeta(columnName="mch_id", dataType="long", dataSize=19, nullable=true)
	@Schema(title = "商户编号", description = "商户编号")
	private long mchId;

	/**
	 * 用户类型
	 */
	@ColumnMeta(columnName="user_type", dataType="int", dataSize=10, nullable=true)
	@Schema(title = "用户类型", description = "用户类型")
	private int userType;

	/**
	 * 通知类型
	 */
	@ColumnMeta(columnName="object_type", dataType="String", dataSize=100, nullable=true)
	@Schema(title = "通知类型", description = "通知类型")
	private String objectType;

	/**
	 * 业务关联ID
	 */
	@ColumnMeta(columnName="object_id", dataType="long", dataSize=19, nullable=true)
	@Schema(title = "业务关联ID", description = "业务关联ID")
	private long objectId;

	/**
	 * 消息内容
	 */
	@ColumnMeta(columnName="notify_subject", dataType="String", dataSize=200, nullable=true)
	@Schema(title = "消息内容", description = "消息内容")
	private String notifySubject;

	/**
	 * 消息内容
	 */
	@ColumnMeta(columnName="notify_content", dataType="String", dataSize=1000, nullable=true)
	@Schema(title = "消息内容", description = "消息内容")
	private String notifyContent;

	/**
	 * 跳转链接
	 */
	@ColumnMeta(columnName="action_url", dataType="String", dataSize=200, nullable=true)
	@Schema(title = "跳转链接", description = "跳转链接")
	private String actionUrl;

	/**
	 * 创建时间
	 */
	@ColumnMeta(columnName="create_date", dataType="java.util.Date", dataSize=19, nullable=false)
	@Schema(title = "创建时间", description = "创建时间")
	private java.util.Date createDate;

	/**
	 * 发送时间
	 */
	@ColumnMeta(columnName="sent_date", dataType="java.util.Date", dataSize=19, nullable=true)
	@Schema(title = "发送时间", description = "发送时间")
	private java.util.Date sentDate;

	/**
	 * 消息状态(0-待发送  1-已发送)
	 */
	@ColumnMeta(columnName="state", dataType="int", dataSize=10, nullable=false)
	@Schema(title = "消息状态(0-待发送  1-已发送)", description = "消息状态(0-待发送  1-已发送)")
	private int state;

	/**
	 * 轻量级状态下更新列表list.
	 */
	private transient Set<String> UPDATED_COLUMN = null;

    /**
	 * 更新的信息.
	 */
        private transient StringBuilder UPDATED_INFO = null;

	/**
	 * 获得更改的字段列表.
	 */
    @Override
	public Set<String> GET_UPDATED_COLUMN() {
        return UPDATED_COLUMN;
	}

	/**
	 * 得到_INFO.
	 */
	@Override
	public String GET_UPDATED_INFO() {
        if (this.UPDATED_INFO == null) {
			return null;
		} else {
            return this.UPDATED_INFO.toString();
		}
	}

    /**
     * 清理_INFO和UPDATED_COLUMN信息.
     */
    public void CLEAR_UPDATED_INFO() {
        UPDATED_COLUMN = null;
        UPDATED_INFO = null;
	}

	/**
	 * 初始化set相关的信息.
	 */
	private void _INIT_UPDATE_INFO() {
		this.UPDATED_COLUMN = new HashSet<String>();
		this.UPDATED_INFO = new StringBuilder("表notify_msg主键\"" + 
		this.id+ "\"更新为:\r\n");
	}


	/**
	 * 获得主键ID。
	 */
	public long getId(){
		return this.id;
	}

	/**
	 * 获得用户ID。
	 */
	public long getUserId(){
		return this.userId;
	}

	/**
	 * 获得运营商编号。
	 */
	public long getSaasId(){
		return this.saasId;
	}

	/**
	 * 获得商户编号。
	 */
	public long getMchId(){
		return this.mchId;
	}

	/**
	 * 获得用户类型。
	 */
	public int getUserType(){
		return this.userType;
	}

	/**
	 * 获得通知类型。
	 */
	public String getObjectType(){
		return this.objectType;
	}

	/**
	 * 获得业务关联ID。
	 */
	public long getObjectId(){
		return this.objectId;
	}

	/**
	 * 获得消息内容。
	 */
	public String getNotifySubject(){
		return this.notifySubject;
	}

	/**
	 * 获得消息内容。
	 */
	public String getNotifyContent(){
		return this.notifyContent;
	}

	/**
	 * 获得跳转链接。
	 */
	public String getActionUrl(){
		return this.actionUrl;
	}

	/**
	 * 获得创建时间。
	 */
	public java.util.Date getCreateDate(){
		return this.createDate;
	}

	/**
	 * 获得发送时间。
	 */
	public java.util.Date getSentDate(){
		return this.sentDate;
	}

	/**
	 * 获得消息状态(0-待发送  1-已发送)。
	 */
	public int getState(){
		return this.state;
	}


	/**
	 * 设置主键ID。
	 */
	public void setId(long id){
		if ((!String.valueOf(this.id).equals(String.valueOf(id)))) {
			if (this.UPDATED_COLUMN == null) {
				_INIT_UPDATE_INFO();
			}
			this.UPDATED_COLUMN.add("id");
			this.UPDATED_INFO.append("id:\"" + this.id+ "\"=>\""
                + id + "\"\r\n");
			this.id = id;
		}
	}

	/**
	 * 设置用户ID。
	 */
	public void setUserId(long userId){
		if ((!String.valueOf(this.userId).equals(String.valueOf(userId)))) {
			if (this.UPDATED_COLUMN == null) {
				_INIT_UPDATE_INFO();
			}
			this.UPDATED_COLUMN.add("user_id");
			this.UPDATED_INFO.append("user_id:\"" + this.userId+ "\"=>\""
                + userId + "\"\r\n");
			this.userId = userId;
		}
	}

	/**
	 * 设置运营商编号。
	 */
	public void setSaasId(long saasId){
		if ((!String.valueOf(this.saasId).equals(String.valueOf(saasId)))) {
			if (this.UPDATED_COLUMN == null) {
				_INIT_UPDATE_INFO();
			}
			this.UPDATED_COLUMN.add("saas_id");
			this.UPDATED_INFO.append("saas_id:\"" + this.saasId+ "\"=>\""
                + saasId + "\"\r\n");
			this.saasId = saasId;
		}
	}

	/**
	 * 设置商户编号。
	 */
	public void setMchId(long mchId){
		if ((!String.valueOf(this.mchId).equals(String.valueOf(mchId)))) {
			if (this.UPDATED_COLUMN == null) {
				_INIT_UPDATE_INFO();
			}
			this.UPDATED_COLUMN.add("mch_id");
			this.UPDATED_INFO.append("mch_id:\"" + this.mchId+ "\"=>\""
                + mchId + "\"\r\n");
			this.mchId = mchId;
		}
	}

	/**
	 * 设置用户类型。
	 */
	public void setUserType(int userType){
		if ((!String.valueOf(this.userType).equals(String.valueOf(userType)))) {
			if (this.UPDATED_COLUMN == null) {
				_INIT_UPDATE_INFO();
			}
			this.UPDATED_COLUMN.add("user_type");
			this.UPDATED_INFO.append("user_type:\"" + this.userType+ "\"=>\""
                + userType + "\"\r\n");
			this.userType = userType;
		}
	}

	/**
	 * 设置通知类型。
	 */
	public void setObjectType(String objectType){
		if ((!String.valueOf(this.objectType).equals(String.valueOf(objectType)))) {
			if (this.UPDATED_COLUMN == null) {
				_INIT_UPDATE_INFO();
			}
			this.UPDATED_COLUMN.add("object_type");
			this.UPDATED_INFO.append("object_type:\"" + this.objectType+ "\"=>\""
                + objectType + "\"\r\n");
			this.objectType = objectType;
		}
	}

	/**
	 * 设置业务关联ID。
	 */
	public void setObjectId(long objectId){
		if ((!String.valueOf(this.objectId).equals(String.valueOf(objectId)))) {
			if (this.UPDATED_COLUMN == null) {
				_INIT_UPDATE_INFO();
			}
			this.UPDATED_COLUMN.add("object_id");
			this.UPDATED_INFO.append("object_id:\"" + this.objectId+ "\"=>\""
                + objectId + "\"\r\n");
			this.objectId = objectId;
		}
	}

	/**
	 * 设置消息内容。
	 */
	public void setNotifySubject(String notifySubject){
		if ((!String.valueOf(this.notifySubject).equals(String.valueOf(notifySubject)))) {
			if (this.UPDATED_COLUMN == null) {
				_INIT_UPDATE_INFO();
			}
			this.UPDATED_COLUMN.add("notify_subject");
			this.UPDATED_INFO.append("notify_subject:\"" + this.notifySubject+ "\"=>\""
                + notifySubject + "\"\r\n");
			this.notifySubject = notifySubject;
		}
	}

	/**
	 * 设置消息内容。
	 */
	public void setNotifyContent(String notifyContent){
		if ((!String.valueOf(this.notifyContent).equals(String.valueOf(notifyContent)))) {
			if (this.UPDATED_COLUMN == null) {
				_INIT_UPDATE_INFO();
			}
			this.UPDATED_COLUMN.add("notify_content");
			this.UPDATED_INFO.append("notify_content:\"" + this.notifyContent+ "\"=>\""
                + notifyContent + "\"\r\n");
			this.notifyContent = notifyContent;
		}
	}

	/**
	 * 设置跳转链接。
	 */
	public void setActionUrl(String actionUrl){
		if ((!String.valueOf(this.actionUrl).equals(String.valueOf(actionUrl)))) {
			if (this.UPDATED_COLUMN == null) {
				_INIT_UPDATE_INFO();
			}
			this.UPDATED_COLUMN.add("action_url");
			this.UPDATED_INFO.append("action_url:\"" + this.actionUrl+ "\"=>\""
                + actionUrl + "\"\r\n");
			this.actionUrl = actionUrl;
		}
	}

	/**
	 * 设置创建时间。
	 */
	public void setCreateDate(java.util.Date createDate){
		if ((!String.valueOf(this.createDate).equals(String.valueOf(createDate)))) {
			if (this.UPDATED_COLUMN == null) {
				_INIT_UPDATE_INFO();
			}
			this.UPDATED_COLUMN.add("create_date");
			this.UPDATED_INFO.append("create_date:\"" + this.createDate+ "\"=>\""
                + createDate + "\"\r\n");
			this.createDate = createDate;
		}
	}

	/**
	 * 设置发送时间。
	 */
	public void setSentDate(java.util.Date sentDate){
		if ((!String.valueOf(this.sentDate).equals(String.valueOf(sentDate)))) {
			if (this.UPDATED_COLUMN == null) {
				_INIT_UPDATE_INFO();
			}
			this.UPDATED_COLUMN.add("sent_date");
			this.UPDATED_INFO.append("sent_date:\"" + this.sentDate+ "\"=>\""
                + sentDate + "\"\r\n");
			this.sentDate = sentDate;
		}
	}

	/**
	 * 设置消息状态(0-待发送  1-已发送)。
	 */
	public void setState(int state){
		if ((!String.valueOf(this.state).equals(String.valueOf(state)))) {
			if (this.UPDATED_COLUMN == null) {
				_INIT_UPDATE_INFO();
			}
			this.UPDATED_COLUMN.add("state");
			this.UPDATED_INFO.append("state:\"" + this.state+ "\"=>\""
                + state + "\"\r\n");
			this.state = state;
		}
	}

	/**
	 * 重载toString方法.
	 */
    @Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("id:\"" + this.id + "\"\r\n");
		sb.append("user_id:\"" + this.userId + "\"\r\n");
		sb.append("saas_id:\"" + this.saasId + "\"\r\n");
		sb.append("mch_id:\"" + this.mchId + "\"\r\n");
		sb.append("user_type:\"" + this.userType + "\"\r\n");
		sb.append("object_type:\"" + this.objectType + "\"\r\n");
		sb.append("object_id:\"" + this.objectId + "\"\r\n");
		sb.append("notify_subject:\"" + this.notifySubject + "\"\r\n");
		sb.append("notify_content:\"" + this.notifyContent + "\"\r\n");
		sb.append("action_url:\"" + this.actionUrl + "\"\r\n");
		sb.append("create_date:\"" + this.createDate + "\"\r\n");
		sb.append("sent_date:\"" + this.sentDate + "\"\r\n");
		sb.append("state:\"" + this.state + "\"\r\n");
		return sb.toString();
	}

}