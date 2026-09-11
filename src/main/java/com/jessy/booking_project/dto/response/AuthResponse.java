package com.jessy.booking_project.dto.response;

/**
 * 登入回應。契約 §5.13 + 附註：形狀是舊版的 {id, account, role} 再多一個 token。
 *
 * <p>這是整份契約唯一刻意偏離舊版的地方，前端要配合改（存 token、之後帶 Authorization header）。
 */
public record AuthResponse(Long id, String account, String role, String token) {
}
