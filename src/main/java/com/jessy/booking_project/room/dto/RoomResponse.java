package com.jessy.booking_project.room.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * 場地的對外形狀。契約 docs/api-contract.md §5.1。
 * component 的宣告順序 = JSON key 的輸出順序，照契約排。
 */
@Schema(
        name = "RoomResponse",
        description = """
                場地資料。列表與詳情回相同形狀。

                除了 `id`，其餘欄位不會是 null：文字沒值是 `""`、數字是 `0`、陣列是 `[]`。
                """)
public record RoomResponse(

        @Schema(description = "場地 id", example = "1",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Long id,

        @Schema(description = "照片網址，可直接放進 `<img src>`。沒有時是空字串。",
                example = "https://res.cloudinary.com/demo/image/upload/v1699999999/booking/rooms/abc123.jpg",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String roomImg,

        @Schema(description = "場地名稱", example = "Studio A — 日光大空間", maxLength = 255,
                requiredMode = Schema.RequiredMode.REQUIRED)
        String title,

        @Schema(description = "場地說明文字", example = "一樓主棚，採光頂天窗，最多可容納 10 人拍攝團隊。",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String desc,

        @Schema(description = "樓層。自由文字，無固定值域。", example = "1F",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String floor,

        @Schema(description = "坪數", example = "80",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Integer area,

        @Schema(description = "可容納人數", example = "10",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Integer capacity,

        @Schema(description = "設備清單。沒有時是空陣列。",
                example = "[\"閃燈組\",\"柔光箱\",\"背景架\",\"造型椅\"]",
                requiredMode = Schema.RequiredMode.REQUIRED)
        List<String> facilities,

        @Schema(description = "三個時段的價格。值是字串不是數字。",
                requiredMode = Schema.RequiredMode.REQUIRED)
        PriceResponse price,

        @Schema(description = "建立時間，格式 `yyyy-MM-dd HH:mm:ss`（不是 ISO 8601）",
                example = "2026-04-16 20:30:00",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String createdAt
) {
}
