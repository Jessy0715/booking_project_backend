package com.jessy.booking_project.booking;

/** 一天固定三個時段。DB 存 name（MORNING），API 用 value（morning）。 */
public enum TimeSlot {
    MORNING("morning"),
    AFTERNOON("afternoon"),
    NIGHT("night");

    private final String value;

    TimeSlot(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static TimeSlot fromValue(String value) {
        for (TimeSlot slot : values()) {
            if (slot.value.equalsIgnoreCase(value)) {
                return slot;
            }
        }
        throw new IllegalArgumentException("timeSlot 必須是 morning / afternoon / night");
    }
}
