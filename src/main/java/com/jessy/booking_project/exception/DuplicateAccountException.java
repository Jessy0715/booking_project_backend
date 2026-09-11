package com.jessy.booking_project.exception;

/** 帳號已被註冊 → 409。訊息照抄舊版。 */
public class DuplicateAccountException extends RuntimeException {

    public DuplicateAccountException() {
        super("此帳號已被使用");
    }
}
