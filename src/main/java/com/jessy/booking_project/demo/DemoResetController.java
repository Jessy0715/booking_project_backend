package com.jessy.booking_project.demo;

import com.jessy.booking_project.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 手動觸發 Demo 重置。給兩種人用：
 * <ul>
 *   <li>外部 cron 服務（例如 cron-job.org）定時打 —— 免費主機會休眠，這也順便叫醒它</li>
 *   <li>你自己想立刻把 Demo 站恢復乾淨時</li>
 * </ul>
 *
 * <p>限 admin（SecurityConfig 設定），且只在 demo profile 存在 ——
 * 正式環境若誤設 demo profile，這支會清光資料，所以環境變數要小心。
 */
@Slf4j
@RestController
@RequestMapping("/api/demo")
@Profile("demo")
@RequiredArgsConstructor
public class DemoResetController {

    private final DemoResetService demoResetService;

    @PostMapping("/reset")
    public ApiResponse<DemoResetResult> reset() {

        return ApiResponse.ok(demoResetService.reset());
    }
}
