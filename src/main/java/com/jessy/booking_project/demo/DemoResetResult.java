package com.jessy.booking_project.demo;

import io.swagger.v3.oas.annotations.media.Schema;

/** 重置結果，回給呼叫者看做了什麼。 */
@Schema(
        name = "DemoResetResult",
        description = "Demo 重置的執行結果。四個數字分別是各步驟實際處理的筆數，用來確認重置有跑完。")
public record DemoResetResult(

        @Schema(description = "刪掉幾筆預約（含原本已軟刪除的）。", example = "3",
                requiredMode = Schema.RequiredMode.REQUIRED)
        int deletedBookings,

        @Schema(description = "刪掉幾個非 admin 帳號。admin 不會被刪。", example = "1",
                requiredMode = Schema.RequiredMode.REQUIRED)
        int deletedUsers,

        @Schema(description = "重建了幾間種子場地。正常情況固定是 10。", example = "10",
                requiredMode = Schema.RequiredMode.REQUIRED)
        int seededRooms,

        @Schema(description = "清掉幾張上傳的圖片。種子場地用的外部網址不算在內。", example = "2",
                requiredMode = Schema.RequiredMode.REQUIRED)
        int deletedImages) {
}
