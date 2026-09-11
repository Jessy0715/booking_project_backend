package com.jessy.booking_project.booking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

/**
 * 建立預約。契約 §5.9。
 *
 * <p>沒有 userId：從 token 拿，不信任 body。
 * <p>三個必填欄位訊息相同 —— 契約規定缺任何一個都回同一句。
 */
public record BookingCreateRequest(

        @NotNull(message = "roomId、date、timeSlot 為必填")
        Long roomId,

        String userName,

        @NotNull(message = "roomId、date、timeSlot 為必填")
        LocalDate date,

        @NotBlank(message = "roomId、date、timeSlot 為必填")
        @Pattern(regexp = "morning|afternoon|night", message = "timeSlot 必須是 morning / afternoon / night")
        String timeSlot,

        String reason
) {
}
