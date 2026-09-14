package com.jessy.booking_project.upload;

import org.springframework.web.multipart.MultipartFile;

/**
 * 圖片儲存。呼叫端只管「丟檔案、拿網址」，不知道檔案實際存在哪。
 *
 * <p>本機開發用 {@link LocalImageStorage}；部署後換成雲端實作（Cloudinary / R2），
 * Controller 一個字都不用改 —— 這就是為什麼要多這一層介面。
 */
public interface ImageStorage {

    /** 存檔並回傳可公開存取的網址。格式不合、存檔失敗都丟 BusinessException。 */
    String store(MultipartFile file);
}
