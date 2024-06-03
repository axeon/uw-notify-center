package uw.notify.center.constant;

/**
 * 消息类型枚举
 *
 * @since 2019/1/28
 */
public enum NotifyType {
    /**
     * 消息非持久化
     */
    INSTANT(0, "即时信息，用户不在线就抛弃"),

    /**
     * 消息持久化
     */
    PERSISTENT(1, "持久信息");

    private int value;
    private String label;

    NotifyType(int value, String label) {
        this.value = value;
        this.label = label;
    }

    public int getValue() {
        return value;
    }

    public String getLabel() {
        return label;
    }
}
