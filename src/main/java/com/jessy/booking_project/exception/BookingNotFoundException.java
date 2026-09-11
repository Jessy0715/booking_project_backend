package com.jessy.booking_project.exception;

/** 找不到預約。訊息文字照抄舊版。 */
public class BookingNotFoundException extends ResourceNotFoundException {

    public BookingNotFoundException() {
        super("預約不存在");
    }
}
