package com.jessy.booking_project.room.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** 三個時段的價格。值是 String 不是 Integer —— 契約範例帶引號。 */
@Schema(
        name = "PriceResponse",
        description = """
                三個時段的租借價格，單位新台幣元。

                **值是字串不是數字**（`"2000"` 而非 `2000`），要運算請先轉型。
                建立/修改場地時送的 `PriceRequest` 則是數字。
                """)
public record PriceResponse(

        @Schema(description = "上午價格。未設定時是 `\"0\"`。", example = "2000",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String morning,

        @Schema(description = "下午價格。未設定時是 `\"0\"`。", example = "3000",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String afternoon,

        @Schema(description = "晚上價格。未設定時是 `\"0\"`。", example = "4000",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String night) {
}
