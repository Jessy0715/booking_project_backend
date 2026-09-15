package com.jessy.booking_project.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * AI 生成場地說明的輸入。
 *
 * <p>防呆就在這兩個 @NotBlank：名稱和照片缺一不可，少一樣直接 400，
 * 根本不會呼叫到 Claude —— 省錢，也避免生出憑空捏造的說明。
 *
 * <p>不用場地 id 而是直接收 title + roomImg：管理員是在「表單還沒送出」時按生成鍵，
 * 那時候場地還不存在資料庫裡。
 */
@Schema(
        name = "RoomDescriptionRequest",
        description = """
                AI 生成場地說明的輸入，兩個欄位都必填。

                不收場地 id，因為按生成鍵時場地通常還沒建立。
                """)
public record RoomDescriptionRequest(

        @NotBlank(message = "請先填寫場地名稱並上傳照片")
        @Schema(description = "場地名稱。名稱越具體，生成品質越好。",
                example = "Studio K — 工業風水泥棚",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String title,

        @NotBlank(message = "請先填寫場地名稱並上傳照片")
        @Schema(description = "照片網址。必須是 `POST /api/uploads/images` 回傳的網址，"
                + "後端要讀取圖片內容交給 AI。",
                example = "https://res.cloudinary.com/demo/image/upload/v1699999999/booking/rooms/abc123.jpg",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String roomImg) {
}
