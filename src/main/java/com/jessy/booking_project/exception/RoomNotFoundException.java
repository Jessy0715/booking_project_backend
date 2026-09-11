package com.jessy.booking_project.exception;

/** 找不到場地。訊息文字照抄舊版，前端會直接顯示。 */
public class RoomNotFoundException extends ResourceNotFoundException {

    public RoomNotFoundException() {
        super("場地不存在");
    }
}
