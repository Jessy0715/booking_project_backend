package com.jessy.booking_project.config;

import com.jessy.booking_project.auth.User;
import com.jessy.booking_project.auth.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 啟動時確保系統至少有一個管理員。
 *
 * <p>register 只會建 user 角色，沒有這段的話清庫後就沒有人能登入後台。
 * 場地與預約的種子資料已移除，改由 API 建立。
 * 階段 11 換成 Flyway 之後這個 class 會被淘汰。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedAdminIfMissing();
    }

    /** 種子管理員。密碼用 BCrypt 存，資料庫裡看不到明文。 */
    private void seedAdminIfMissing() {
        if (userRepository.existsByAccount("admin")) {
            return;
        }
        userRepository.save(User.builder()
                .account("admin")
                .password(passwordEncoder.encode("admin1234"))
                .role(User.ROLE_ADMIN)
                .build());
        log.info("種子管理員已建立：admin");
    }
}
