package com.jessy.booking_project.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 使用者的對外形狀。契約 §5.12：{"id":3,"account":"jessy","role":"user"}
 *
 * <p><b>這個 record 是 password 唯一的防線。</b>
 * Entity 有 password 欄位，只要這裡不宣告它，它就永遠出不去。
 */
@Schema(
        name = "UserResponse",
        description = "使用者的公開資訊，註冊成功時回傳。不包含密碼，也沒有 token。")
public record UserResponse(

        @Schema(description = "使用者 id", example = "3",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Long id,

        @Schema(description = "登入帳號", example = "jessy",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String account,

        @Schema(description = "角色。透過註冊建立的一律是 `user`。", example = "user",
                allowableValues = {"user", "admin"},
                requiredMode = Schema.RequiredMode.REQUIRED)
        String role

) {
}
