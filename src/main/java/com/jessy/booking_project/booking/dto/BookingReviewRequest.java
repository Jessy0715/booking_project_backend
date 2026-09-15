package com.jessy.booking_project.booking.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** 審核。契約 §5.10：只接受 approved / rejected。 */
@Schema(
        name = "BookingReviewRequest",
        description = "admin 審核的結果。只有 `pending` 的預約能被審核，已審核過的回 400。")
public record BookingReviewRequest(

        @NotBlank(message = "status 只能是 approved 或 rejected")
        @Pattern(regexp = "approved|rejected", message = "status 只能是 approved 或 rejected")
        @Schema(description = "審核結果。`approved` 繼續佔用時段，`rejected` 釋出時段。"
                + "送 `pending` 或其他值回 400。",
                example = "approved",
                allowableValues = {"approved", "rejected"},
                requiredMode = Schema.RequiredMode.REQUIRED)
        String status
) {
}
