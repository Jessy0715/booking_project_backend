package com.jessy.booking_project.ai.dto;

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
public record RoomDescriptionRequest(

        @NotBlank(message = "請先填寫場地名稱並上傳照片")
        String title,

        @NotBlank(message = "請先填寫場地名稱並上傳照片")
        String roomImg) {
}
