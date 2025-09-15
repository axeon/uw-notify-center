package uw.notify.center;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import uw.common.app.AppBootStrap;

@SpringBootApplication
@EnableDiscoveryClient
public class UwNotifyCenterApplication {

    public static void main(String[] args) {
        AppBootStrap.run(UwNotifyCenterApplication.class, args);
    }

}
