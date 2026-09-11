package com.jessy.booking_project.booking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** 審核。契約 §5.10：只接受 approved / rejected。 */
public record BookingReviewRequest(

        @NotBlank(message = "status 只能是 approved 或 rejected")
        @Pattern(regexp = "approved|rejected", message = "status 只能是 approved 或 rejected")
        String status
) {
}
