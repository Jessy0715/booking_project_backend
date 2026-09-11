package com.jessy.booking_project.auth.dto;

import jakarta.validation.constraints.NotBlank;

/** 登入請求。契約 §5.13。 */
public record LoginRequest(

        @NotBlank(message = "帳號與密碼為必填")
        String account,

        @NotBlank(message = "帳號與密碼為必填")
        String password
) {
}
