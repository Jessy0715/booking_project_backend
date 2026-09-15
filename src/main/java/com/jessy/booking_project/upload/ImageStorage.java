package com.jessy.booking_project.upload;

/**
 * 圖片儲存。只負責 I/O：「存 bytes、回網址、刪網址」。
 *
 * <p>驗證、縮圖等規則都在 {@link UploadService}，所以不管換成哪種實作，
 * 收到的都是已經處理好的 bytes，行為一致。
 *
 * <p>本機開發用 {@link LocalImageStorage}；部署後換成 {@link CloudinaryImageStorage}，
 * Controller / Service 一個字都不用改 —— 這就是為什麼要多這一層介面。
 */
public interface ImageStorage {

    /**
     * 存檔並回傳可公開存取的網址。
     *
     * @param content   已驗證、已處理的圖片內容
     * @param extension 副檔名（不含點），例如 "jpg"
     */
    String store(byte[] content, String extension);

    /**
     * 這個網址是不是本 storage 存出去的。
     *
     * <p>種子資料用的是 Unsplash 網址，不是我們存的，**絕對不能去刪**。
     * 切換過儲存方式時，舊網址也會落在這裡被判定為「不是我的」而略過。
     */
    boolean owns(String url);

    /**
     * 刪除這個網址對應的檔案。找不到檔案視為已刪除，不算錯誤。
     *
     * <p>呼叫端應先用 {@link #owns(String)} 過濾。刪不掉只記 log，不丟例外 ——
     * 刪圖失敗不該讓「刪場地」這個使用者動作失敗，剩下的孤兒檔由每日排程清。
     */
    void delete(String url);
}
