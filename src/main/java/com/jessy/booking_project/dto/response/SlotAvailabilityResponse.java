package com.jessy.booking_project.dto.response;

/**
 * 某棚某天三個時段的狀態。契約 §5.2。值域：available | pending | approved。
 *
 * <p>教學點 5：這個形狀不對應任何一張表、任何一個 Entity。
 * 它是為了回答「這天還有哪些時段可以借」而算出來的。
 */
public record SlotAvailabilityResponse(
        String morning,
        String afternoon,
        String night) {

    /** 沒有任何有效預約時的值。 */
    public static final String AVAILABLE = "available";
}
