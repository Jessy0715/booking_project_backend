package com.jessy.booking_project.room.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

/**
 * 建立場地的請求。契約 docs/api-contract.md §5.4。
 *
 * <p>只有 title 必填，其餘由 Mapper 補預設值。
 * capacity/area/floor/facilities 是舊版建立時漏掉的四個欄位，這裡補齊。
 */
public record RoomCreateRequest(

        @NotBlank(message = "title 為必填")
        String title,

        String roomImg,
        String desc,
        String floor,
        Integer area,
        Integer capacity,
        List<String> facilities,
        PriceRequest price
) {
}
