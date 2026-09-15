package com.jessy.booking_project.demo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * 每日定時重置。
 *
 * <p>@EnableScheduling 放在這裡而不是主程式：只有 demo profile 啟動時才會開排程，
 * 本機開發連排程器都不會建立。
 *
 * <p>排程跑在應用程式自己的執行緒裡，**不是系統的 crontab** ——
 * 應用程式關掉，排程就停了，不會有東西殘留在機器上。
 *
 * <p>關掉排程但保留 /api/demo/reset 端點：把 app.demo.reset.cron 設成 "-"（Spring 的停用值）。
 */
@Slf4j
@Component
@Profile("demo")
@EnableScheduling
@RequiredArgsConstructor
public class DemoResetJob {

    private final DemoResetService demoResetService;

    @Value("${app.demo.reset.cron}")
    private String cron;

    @PostConstruct
    void logSchedule() {
        if ("-".equals(cron)) {
            log.info("Demo 每日重置：已停用（仍可手動呼叫 POST /api/demo/reset）");
        } else {
            log.info("Demo 每日重置：排程 {}（Asia/Taipei）", cron);
        }
    }

    /**
     * 預設每天凌晨 4 點（台北時間）—— 選最少人在用的時間，
     * 因為重置當下正在操作的人會失敗一次。
     *
     * <p>⚠️ 免費方案的主機閒置會休眠，休眠中這個排程不會跑。
     * 真正可靠的觸發方式是外部 cron 服務定時打 POST /api/demo/reset（順便叫醒主機），
     * 這個 @Scheduled 只是備援。
     */
    @Scheduled(cron = "${app.demo.reset.cron}", zone = "Asia/Taipei")
    public void resetDaily() {
        log.info("每日重置排程觸發");
        demoResetService.reset();
    }
}
