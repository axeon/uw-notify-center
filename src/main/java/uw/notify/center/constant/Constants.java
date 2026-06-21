package uw.notify.center.constant;

/**
 * notify-center 全局常量。
 *
 * @author axeon
 */
public class Constants {

    /**
     * 工具类，禁止实例化。
     */
    private Constants() {
    }

    /**
     * 跨实例广播通知使用的 Redis Pub/Sub 通道名。
     * <p>
     * notify-center 所有实例均订阅该通道；当某实例需要投递的目标用户不在本地连接池时，
     * 将消息发布到此通道，由持有该用户 SSE 连接的实例完成最终投递。
     */
    public static final String REDIS_NOTIFY_CHANNEL = "uw-notify";
}
