package com.jessy.booking_project.common;

import io.swagger.v3.oas.annotations.media.Schema;

/** 分頁資訊。契約：{"page":1,"pageSize":5,"total":10,"totalPages":2}，page 從 1 開始。 */
@Schema(name = "PaginationResponse", description = "分頁資訊。只有列表型端點會帶。")
public record PaginationResponse(

        @Schema(description = "目前第幾頁，從 1 開始。", example = "1", minimum = "1",
                requiredMode = Schema.RequiredMode.REQUIRED)
        int page,

        @Schema(description = "每頁幾筆。這是後端實際採用的值，不一定等於你送出的值。",
                example = "5", minimum = "1", maximum = "100",
                requiredMode = Schema.RequiredMode.REQUIRED)
        int pageSize,

        @Schema(description = "符合條件的總筆數（不是這一頁的筆數）", example = "10",
                requiredMode = Schema.RequiredMode.REQUIRED)
        long total,

        @Schema(description = "總頁數。總筆數為 0 時是 0。", example = "2",
                requiredMode = Schema.RequiredMode.REQUIRED)
        int totalPages) {
}
