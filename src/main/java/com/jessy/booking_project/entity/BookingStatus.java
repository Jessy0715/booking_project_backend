package com.jessy.booking_project.entity;

/** 預約狀態。階段 9 會在這上面做狀態機（只有 PENDING 能被審核）。 */
public enum BookingStatus {
    PENDING("pending"),
    APPROVED("approved"),
    REJECTED("rejected");

    private final String value;

    BookingStatus(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static BookingStatus fromValue(String value) {
        for (BookingStatus status : values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("status 必須是 pending / approved / rejected");
    }
}
