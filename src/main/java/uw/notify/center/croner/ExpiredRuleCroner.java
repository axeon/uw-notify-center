package uw.notify.center.croner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uw.dao.DaoFactory;
import uw.dao.TransactionException;

import java.util.Date;

/**
 * 过期规则定时清除。
 */
@Component
@EnableScheduling
public class ExpiredRuleCroner {

    private static final Logger log = LoggerFactory.getLogger(ExpiredRuleCroner.class);
    private DaoFactory dao = DaoFactory.getInstance();

    /**
     * 删除过期30天的数据。
     */
    private long TIME_DIFF = 30 * 24 * 60 * 60 * 1000L;

    /**
     * 清理过期规则，每天1点清除。
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void clearExpiredRule() {

        //清除过期数据。
        try {
            dao.executeCommand("delete from notify_msg where create_date<?", new Object[]{new Date(System.currentTimeMillis() - TIME_DIFF)});
        } catch (TransactionException e) {
            log.error(e.getMessage(), e);
        }

    }
}
