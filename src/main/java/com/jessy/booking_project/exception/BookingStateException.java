package com.jessy.booking_project.exception;

/** 預約目前的狀態不允許這個操作 → 400。例如取消已核准的、重複審核。 */
public class BookingStateException extends RuntimeException {

    public BookingStateException(String message) {
        super(message);
    }
}
