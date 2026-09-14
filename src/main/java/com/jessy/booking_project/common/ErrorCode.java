package com.jessy.booking_project.common;

import org.springframework.http.HttpStatus;

/**
 * 全站錯誤碼。狀態碼與中文訊息集中在這裡 —— 契約要求「訊息照抄舊版」，一張表最好對照。
 *
 * <p>加新錯誤只要加一行，不用開新 class、不用加 handler。
 */
public enum ErrorCode {

    // 404
    ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "場地不存在"),
    BOOKING_NOT_FOUND(HttpStatus.NOT_FOUND, "預約不存在"),
    ROUTE_NOT_FOUND(HttpStatus.NOT_FOUND, "找不到此路由"),

    // 400
    BOOKING_ALREADY_REVIEWED(HttpStatus.BAD_REQUEST, "此預約已審核完畢，無法再次變更"),
    BOOKING_NOT_PENDING(HttpStatus.BAD_REQUEST, "只能取消審核中 (pending) 的預約"),
    UNREADABLE_BODY(HttpStatus.BAD_REQUEST, "請求內容格式錯誤"),
    UPLOAD_EMPTY(HttpStatus.BAD_REQUEST, "請選擇要上傳的圖片"),
    UPLOAD_TYPE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "只支援 jpg / png / webp 格式的圖片"),

    // 413
    UPLOAD_TOO_LARGE(HttpStatus.PAYLOAD_TOO_LARGE, "圖片大小不能超過 5 MB"),

    // 401 / 403
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "帳號或密碼錯誤"),
    BOOKING_NOT_OWNER(HttpStatus.FORBIDDEN, "不能取消別人的預約"),

    // 409
    DUPLICATE_ACCOUNT(HttpStatus.CONFLICT, "此帳號已被使用"),
    BOOKING_SLOT_APPROVED(HttpStatus.CONFLICT, "該時段已核准，無法重複預約"),
    BOOKING_SLOT_PENDING(HttpStatus.CONFLICT, "該時段已有人申請中，請選擇其他時間"),

    // 500
    UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "圖片儲存失敗"),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "伺服器內部錯誤");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus status() {
        return status;
    }

    public String message() {
        return message;
    }
}
