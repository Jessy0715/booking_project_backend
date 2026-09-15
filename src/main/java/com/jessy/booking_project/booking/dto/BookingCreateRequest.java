package com.jessy.booking_project.booking.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(
        name = "BookingCreateRequest",
        description = """
                送出預約申請。

                **沒有 `userId` 欄位**，預約人身分取自 token，送了也會被忽略。

                必填：`roomId`、`date`、`timeSlot`。缺任一個回同一句錯誤，無法分辨缺哪個。
                """)
public record BookingCreateRequest(

        @NotNull(message = "roomId、date、timeSlot 為必填")
        @Schema(description = "要預約哪一間棚", example = "1",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Long roomId,

        @Schema(description = "預約人姓名（顯示用，與登入帳號無關）", example = "王小明",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String userName,

        @NotNull(message = "roomId、date、timeSlot 為必填")
        @Schema(description = "預約日期，格式 `yyyy-MM-dd`", example = "2026-09-15",
                requiredMode = Schema.RequiredMode.REQUIRED)
        LocalDate date,

        @NotBlank(message = "roomId、date、timeSlot 為必填")
        @Pattern(regexp = "morning|afternoon|night", message = "timeSlot 必須是 morning / afternoon / night")
        @Schema(description = "時段。只能是三個小寫字串之一，大寫也不行。", example = "morning",
                allowableValues = {"morning", "afternoon", "night"},
                requiredMode = Schema.RequiredMode.REQUIRED)
        String timeSlot,

        @Schema(description = "用途說明，給 admin 審核參考", example = "商品拍攝",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String reason
) {
}
