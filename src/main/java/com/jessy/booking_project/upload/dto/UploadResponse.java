package com.jessy.booking_project.upload.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** 上傳成功回的網址。前端拿去塞進場地表單的 roomImg。 */
@Schema(name = "UploadResponse", description = "圖片上傳成功後的網址。")
public record UploadResponse(

        @Schema(description = "圖片的公開網址，可直接用於 `<img src>`。"
                + "網址格式依後端儲存模式而不同，前端不要做任何假設。",
                example = "https://res.cloudinary.com/demo/image/upload/v1699999999/booking/rooms/abc123.jpg",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String url) {
}
