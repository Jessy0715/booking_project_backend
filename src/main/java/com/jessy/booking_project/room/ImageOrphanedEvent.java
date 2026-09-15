package com.jessy.booking_project.room;

/**
 * 「這個網址的圖片已經沒有人在用了」。場地被刪、或圖片被換掉時發出。
 *
 * <p>room 模組只負責宣告「這張圖沒人用了」，不知道圖存在哪、也不負責刪 ——
 * 那是 upload 模組的事。兩個模組因此不互相依賴。
 */
public record ImageOrphanedEvent(String imageUrl) {
}
