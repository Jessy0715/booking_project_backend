package com.jessy.booking_project.room.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 下拉選單用的場地選項。只有 id 與顯示文字。
 *
 * <p>這是 Spring Data 的 <b>Projection</b>：record 的欄位名稱對應 Entity 屬性名，
 * Spring Data 會自動生成只 SELECT 這兩欄的 SQL，不撈整張表。
 *
 * <p>與 RoomResponse 的關係：不是「精簡版」，是「不同用途的形狀」。
 * 列表頁要看價格、設施；下拉選單只要選得出來。
 */
@Schema(name = "RoomOptionResponse", description = "下拉選單用的場地選項")
public record RoomOptionResponse(

        @Schema(description = "場地 id，選中後送到預約 API 的 `roomId`", example = "1",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Long id,

        @Schema(description = "顯示文字", example = "Studio A — 日光大空間",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String title) {
}
