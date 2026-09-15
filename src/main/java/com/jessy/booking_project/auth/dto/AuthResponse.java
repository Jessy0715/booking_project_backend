package com.jessy.booking_project.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 登入回應。契約 §5.13 + 附註：形狀是舊版的 {id, account, role} 再多一個 token。
 *
 * <p>這是整份契約唯一刻意偏離舊版的地方，前端要配合改（存 token、之後帶 Authorization header）。
 */
@Schema(
        name = "AuthResponse",
        description = """
                登入成功後的身分資訊與 token。

                `role` 只供前端畫面判斷用，真正的權限檢查在後端驗 token，改它不會得到權限。
                """)
public record AuthResponse(

        @Schema(description = "使用者 id。查「我的預約」時當 `userId` 參數用。", example = "3",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Long id,

        @Schema(description = "登入帳號", example = "jessy",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String account,

        @Schema(description = "角色", example = "user",
                allowableValues = {"user", "admin"},
                requiredMode = Schema.RequiredMode.REQUIRED)
        String role,

        @Schema(description = "JWT，有效期 1 小時。之後請求帶 `Authorization: Bearer <這個字串>`。",
                example = "eyJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJib29raW5nLXByb2plY3QifQ.xxxxx",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String token) {
}
