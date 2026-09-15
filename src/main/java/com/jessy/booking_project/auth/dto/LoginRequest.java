package com.jessy.booking_project.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/** 登入請求。契約 §5.13。 */
@Schema(name = "LoginRequest", description = "登入用的帳號密碼，兩個都必填。")
public record LoginRequest(

        @NotBlank(message = "帳號與密碼為必填")
        @Schema(description = "登入帳號。前後空白會自動去除。", example = "admin",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String account,

        @NotBlank(message = "帳號與密碼為必填")
        @Schema(description = "密碼。區分大小寫。", example = "admin1234", format = "password",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String password
) {
}
