package com.jessy.booking_project.room.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

/**
 * 建立場地的請求。契約 docs/api-contract.md §5.4。
 *
 * <p>只有 title 必填，其餘由 Mapper 補預設值。
 * capacity/area/floor/facilities 是舊版建立時漏掉的四個欄位，這裡補齊。
 */
@Schema(
        name = "RoomCreateRequest",
        description = """
                建立或修改場地的輸入，POST 與 PUT 共用。

                只有 `title` 必填，其餘省略時補預設值（文字 `""`、數字 `0`、陣列 `[]`）。

                `price` 送**數字**，但回應的 `PriceResponse` 是**字串**。
                """)
public record RoomCreateRequest(

        @NotBlank(message = "title 為必填")
        @Schema(description = "場地名稱。唯一必填欄位。", example = "Studio K — 工業風水泥棚",
                maxLength = 255, requiredMode = Schema.RequiredMode.REQUIRED)
        String title,

        @Schema(description = "照片網址。建議填 `POST /api/uploads/images` 回傳的 url。",
                example = "https://res.cloudinary.com/demo/image/upload/v1699999999/booking/rooms/abc123.jpg",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String roomImg,

        @Schema(description = "場地說明文字", example = "水泥原牆與大面窗光，適合工業風人像與服裝拍攝。",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String desc,

        @Schema(description = "樓層。自由文字，無固定值域。", example = "B1",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String floor,

        @Schema(description = "坪數", example = "55",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        Integer area,

        @Schema(description = "可容納人數", example = "8",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        Integer capacity,

        @Schema(description = "設備清單。後端以逗號串接儲存，**名稱本身不能含逗號**。",
                example = "[\"閃燈組\",\"柔光箱\",\"背景架\"]",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        List<String> facilities,

        @Schema(description = "三個時段的價格，送數字。整個物件可省略。",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        PriceRequest price
) {
}
