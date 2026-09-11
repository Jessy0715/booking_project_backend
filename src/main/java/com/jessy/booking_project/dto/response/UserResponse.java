package com.jessy.booking_project.dto.response;

/**
 * 使用者的對外形狀。契約 §5.12：{"id":3,"account":"jessy","role":"user"}
 *
 * <p><b>這個 record 是 password 唯一的防線。</b>
 * Entity 有 password 欄位，只要這裡不宣告它，它就永遠出不去。
 */
public record UserResponse(

        Long id,
        String account,
        String role

) {
}
