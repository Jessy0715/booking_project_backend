package com.jessy.booking_project.upload;

/**
 * 圖片儲存。只負責 I/O：「存 bytes、回網址」。
 *
 * <p>驗證、縮圖等規則都在 {@link UploadService}，所以不管換成哪種實作，
 * 收到的都是已經處理好的 bytes，行為一致。
 *
 * <p>本機開發用 {@link LocalImageStorage}；部署後換成雲端實作（Cloudinary），
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
}
