package com.jessy.booking_project.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** AI 生成的場地說明。前端拿去填進表單的 desc 欄位，使用者還能自己改。 */
@Schema(
        name = "RoomDescriptionResponse",
        description = "AI 生成的場地說明。後端不會自動儲存，請填進表單讓使用者可繼續編輯。")
public record RoomDescriptionResponse(

        @Schema(description = "生成的說明文字。目標 60 字以內但**不保證**，後端不截斷。",
                example = "水泥原牆搭配大面窗光，空間開闊，適合工業風人像與服裝形象拍攝。",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String description) {
}
