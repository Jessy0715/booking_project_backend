package com.jessy.booking_project.config;

import com.jessy.booking_project.security.JwtAuthenticationFilter;
import com.jessy.booking_project.security.RestAccessDeniedHandler;
import com.jessy.booking_project.security.RestAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/** 7c 版本：依契約分成公開 / 需登入 / 需 admin 三類。 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RestAuthenticationEntryPoint authenticationEntryPoint;
    private final RestAccessDeniedHandler accessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 純 API + JWT，沒有瀏覽器表單送出，不需要 CSRF token
                .csrf(csrf -> csrf.disable())
                // 沿用 CorsConfig 的設定
                .cors(Customizer.withDefaults())
                // 不建 session，每個請求靠 token 自證身分
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Filter 層丟出的 401/403，@RestControllerAdvice 攔不到，要在這裡接
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))

                .authorizeHttpRequests(auth -> auth

                        // 需登入：logout 語意上代表「你正處於登入狀態」，沒帶 token / token 過期直接 401。
                        // 順序要排在下面的 /api/auth/** permitAll 之前 —— matcher 先符合先套用。
                        .requestMatchers(HttpMethod.POST, "/api/auth/logout").authenticated()
                        // 公開：登入註冊本身不能要求登入，否則永遠進不來
                        .requestMatchers("/api/auth/**").permitAll()
                        // 公開：首頁、場地介紹頁、預約日曆都是未登入可看
                        .requestMatchers(HttpMethod.GET, "/api/rooms/**").permitAll()
                        // 公開：API 文件
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        // 後台：場地的新增／修改／刪除
                        .requestMatchers(HttpMethod.POST, "/api/rooms").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/rooms/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/rooms/**").hasRole("ADMIN")
                        // 後台：審核預約
                        .requestMatchers(HttpMethod.PATCH, "/api/bookings/**").hasRole("ADMIN")
                        // 其餘一律需要登入
                        .anyRequest().authenticated())

                // 放在帳密驗證 filter 之前：先讓 token 建立身分，後面規則才有東西可以判斷
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
