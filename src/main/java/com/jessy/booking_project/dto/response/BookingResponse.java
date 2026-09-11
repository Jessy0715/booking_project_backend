package com.jessy.booking_project.dto.response;



/**
 * 預約的對外形狀。契約 §5.7。
 *
 * <p>roomTitle 不在 bookings 表裡 —— 它來自關聯的 Room。這是「跨表組合欄位」。
 */
public record BookingResponse(
        Long id,
        Long roomId,
        String roomTitle,
        Long userId,
        String userName,
        String date,
        String timeSlot,
        String reason,
        String status,
        String createdAt
) {
}
