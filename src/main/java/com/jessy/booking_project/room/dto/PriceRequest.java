package com.jessy.booking_project.room.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** 建立/修改場地時傳入的價格。值是數字，跟回應的字串不一樣（契約如此）。 */
@Schema(
        name = "PriceRequest",
        description = """
                建立或修改場地時送的價格，單位元。

                送進來是**數字**，查詢時回的是**字串**。三個欄位都可省略，省略存成 0。
                """)
public record PriceRequest(

        @Schema(description = "上午價格", example = "2000", minimum = "0",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        Integer morning,

        @Schema(description = "下午價格", example = "3000", minimum = "0",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        Integer afternoon,

        @Schema(description = "晚上價格", example = "4000", minimum = "0",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        Integer night) {
}
