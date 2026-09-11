package com.jessy.booking_project.room.dto;

/** 三個時段的價格。值是 String 不是 Integer —— 契約範例帶引號。 */
public record PriceResponse(
        String morning,
        String afternoon,
        String night) {
}
