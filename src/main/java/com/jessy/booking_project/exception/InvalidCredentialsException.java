package com.jessy.booking_project.exception;

/**
 * 帳號或密碼錯誤 → 401。
 *
 * <p>訊息刻意不區分「帳號不存在」與「密碼錯誤」—— 區分了等於告訴攻擊者哪些帳號存在。
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("帳號或密碼錯誤");
    }
}
