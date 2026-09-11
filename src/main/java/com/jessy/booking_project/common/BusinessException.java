package com.jessy.booking_project.common;

/**
 * 業務例外。只帶一個 ErrorCode，狀態碼與訊息都從那裡來。
 *
 * <p>用法：{@code throw new BusinessException(ErrorCode.ROOM_NOT_FOUND);}
 */
public class BusinessException extends RuntimeException {

    private final ErrorCode code;

    public BusinessException(ErrorCode code) {
        super(code.message());
        this.code = code;
    }

    public ErrorCode getCode() {
        return code;
    }
}
