package com.jessy.booking_project.booking.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 某棚某天三個時段的狀態。契約 §5.2。值域：available | pending | approved。
 *
 * <p>教學點 5：這個形狀不對應任何一張表、任何一個 Entity。
 * 它是為了回答「這天還有哪些時段可以借」而算出來的。
 */
@Schema(
        name = "SlotAvailabilityResponse",
        description = """
                某棚某天三個時段的可用狀態。三個欄位一定都有值。

                `available` 可選；`pending` 有人申請中；`approved` 已核准。
                被退回（rejected）的預約不佔位，會顯示 `available`。
                """)
public record SlotAvailabilityResponse(

        @Schema(description = "上午時段狀態", example = "available",
                allowableValues = {"available", "pending", "approved"},
                requiredMode = Schema.RequiredMode.REQUIRED)
        String morning,

        @Schema(description = "下午時段狀態", example = "pending",
                allowableValues = {"available", "pending", "approved"},
                requiredMode = Schema.RequiredMode.REQUIRED)
        String afternoon,

        @Schema(description = "晚上時段狀態", example = "approved",
                allowableValues = {"available", "pending", "approved"},
                requiredMode = Schema.RequiredMode.REQUIRED)
        String night) {

    /** 沒有任何有效預約時的值。 */
    public static final String AVAILABLE = "available";
}
