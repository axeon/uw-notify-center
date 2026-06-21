package uw.notify.center;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import uw.common.app.AppBootStrap;

/**
 * notify-center 启动入口。
 * <p>
 * 基于 SSE（Server-Sent Events）的 Web 实时通知中心，支持单实例内用户级推送，
 * 并通过 Redis Pub/Sub 实现跨实例投递路由。
 *
 * @author axeon
 */
@SpringBootApplication
@EnableDiscoveryClient
public class UwNotifyCenterApplication {

    /**
     * 应用启动入口。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        AppBootStrap.run(UwNotifyCenterApplication.class, args);
    }

}
