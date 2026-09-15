package com.jessy.booking_project.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * 註冊請求。契約 §5.12。
 *
 * <p>兩個欄位的訊息刻意相同 —— 契約規定不論缺哪一個都回「帳號與密碼為必填」。
 */
@Schema(
        name = "RegisterRequest",
        description = "註冊新帳號，兩個欄位都必填。建立出來的一律是 `user` 角色。")
public record RegisterRequest(

        @NotBlank(message = "帳號與密碼為必填")
        @Schema(description = "登入帳號，不可重複。前後空白會自動去除。", example = "jessy",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String account,

        @NotBlank(message = "帳號與密碼為必填")
        @Schema(description = "密碼。目前沒有長度或複雜度限制。", example = "my-password-1234",
                format = "password", requiredMode = Schema.RequiredMode.REQUIRED)
        String password
) {
}
