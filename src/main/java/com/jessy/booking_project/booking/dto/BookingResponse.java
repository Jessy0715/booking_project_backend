package com.jessy.booking_project.booking.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 預約的對外形狀。契約 §5.7。
 *
 * <p>roomTitle 不在 bookings 表裡 —— 它來自關聯的 Room。這是「跨表組合欄位」。
 */
@Schema(
        name = "BookingResponse",
        description = "一筆預約。除了 `userId` 可能為 null，其餘欄位都有值（文字沒填是空字串）。")
public record BookingResponse(

        @Schema(description = "預約 id", example = "12",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Long id,

        @Schema(description = "借哪一間棚", example = "1",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Long roomId,

        @Schema(description = "場地名稱。後端已 join 好，前端不用另外查場地 API。",
                example = "Studio A — 日光大空間",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String roomTitle,

        @Schema(description = "預約人 id。來自 token。舊資料可能是 null。", example = "2",
                nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        Long userId,

        @Schema(description = "預約人姓名（顯示用，與帳號無關）", example = "王小明",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String userName,

        @Schema(description = "預約日期，格式 `yyyy-MM-dd`", example = "2026-09-15",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String date,

        @Schema(description = "時段", example = "morning",
                allowableValues = {"morning", "afternoon", "night"},
                requiredMode = Schema.RequiredMode.REQUIRED)
        String timeSlot,

        @Schema(description = "用途說明", example = "商品拍攝",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String reason,

        @Schema(description = "審核狀態。`pending` / `approved` 佔用時段，`rejected` 不佔用。",
                example = "pending",
                allowableValues = {"pending", "approved", "rejected"},
                requiredMode = Schema.RequiredMode.REQUIRED)
        String status,

        @Schema(description = "建立時間，格式 `yyyy-MM-dd HH:mm:ss`。列表依此由新到舊排序。",
                example = "2026-09-09 10:22:00",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String createdAt
) {
}
