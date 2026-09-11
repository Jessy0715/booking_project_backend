package com.jessy.booking_project.exception;

/** 同棚同天同時段已有有效預約 → 409。訊息依對方的狀態不同，由呼叫端決定。 */
public class BookingConflictException extends RuntimeException {

    public static final String APPROVED_MESSAGE = "該時段已核准，無法重複預約";
    public static final String PENDING_MESSAGE = "該時段已有人申請中，請選擇其他時間";

    public BookingConflictException(String message) {
        super(message);
    }
}
