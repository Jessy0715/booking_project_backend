package com.jessy.booking_project.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * 註冊請求。契約 §5.12。
 *
 * <p>兩個欄位的訊息刻意相同 —— 契約規定不論缺哪一個都回「帳號與密碼為必填」。
 */
public record RegisterRequest(

        @NotBlank(message = "帳號與密碼為必填")
        String account,

        @NotBlank(message = "帳號與密碼為必填")
        String password
) {
}
