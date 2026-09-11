package com.jessy.booking_project.room.dto;

import java.util.List;

/**
 * 場地的對外形狀。契約 docs/api-contract.md §5.1。
 * component 的宣告順序 = JSON key 的輸出順序，照契約排。
 */
public record RoomResponse(
        Long id,
        String roomImg,
        String title,
        String desc,
        String floor,
        Integer area,
        Integer capacity,
        List<String> facilities,
        PriceResponse price,
        String createdAt
) {
}
