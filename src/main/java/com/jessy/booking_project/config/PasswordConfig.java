package com.jessy.booking_project.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 密碼雜湊器。
 *
 * <p>只依賴 spring-security-crypto，還沒有整包 Security starter ——
 * 所以此時所有端點仍然公開，不會被 401 擋住。
 */
@Configuration
public class PasswordConfig {

    /**
     * BCrypt 內建 salt，同一個密碼每次雜湊結果都不同，天生防彩虹表。
     *
     * <p>strength 預設 10（約 100ms）。刻意慢，讓暴力破解划不來。
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
