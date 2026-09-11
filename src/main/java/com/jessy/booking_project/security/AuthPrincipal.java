package com.jessy.booking_project.security;

/**
 * 登入者身分，從 JWT claims 還原，不查資料庫。
 *
 * <p>Controller 用 {@code @AuthenticationPrincipal AuthPrincipal me} 就能拿到。
 * 階段 9 檢查「這筆預約是不是本人的」會用到 uid。
 */
public record AuthPrincipal(Long uid, String account, String role) {
}
