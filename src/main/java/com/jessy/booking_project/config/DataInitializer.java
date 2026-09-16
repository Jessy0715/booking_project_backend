package com.jessy.booking_project.config;

import com.jessy.booking_project.auth.AuthService;
import com.jessy.booking_project.auth.User;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 啟動時確保系統有可用的帳號。
 *
 * <p>register 只會建 user 角色，沒有這段的話清庫後就沒有人能登入後台。
 * 場地與預約的種子資料由 Demo 重置流程負責（見 demo/DemoResetService）。
 * 階段 11 換成 Flyway 之後這個 class 會被淘汰。
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    /** Demo 站的公開帳號，寫在 README 給訪客用。 */
    public static final String DEMO_USER_ACCOUNT = "demo";
    public static final String DEMO_USER_PASSWORD = "demo1234";

    private final AuthService authService;

    @Override
    public void run(String... args) {
        // 密碼寫死是 Demo 站的刻意選擇：帳號本來就是公開的，每天還會重置
        authService.ensureUser("admin", "admin1234", User.ROLE_ADMIN);
        authService.ensureUser(DEMO_USER_ACCOUNT, DEMO_USER_PASSWORD, User.ROLE_USER);
    }
}
