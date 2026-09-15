package com.jessy.booking_project.upload;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.jessy.booking_project.common.BusinessException;
import com.jessy.booking_project.common.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * 存到 Cloudinary（部署用）。
 *
 * <p>跟 LocalImageStorage 一樣只做 I/O：拿到 bytes → 上傳 → 回網址。
 * 驗證、縮圖都在 UploadService 做完了，這裡拿到的已經是 ≤ 1280px 的 JPEG。
 *
 * <p>只在 app.upload.storage=cloudinary 時啟用，且必須有 CLOUDINARY_URL，否則啟動直接失敗 ——
 * 寧願部署時馬上發現設定漏了，不要等第一個人上傳才爆。
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "app.upload.storage", havingValue = "cloudinary")
public class CloudinaryImageStorage implements ImageStorage {

    private final Cloudinary cloudinary;
    private final String folder;

    public CloudinaryImageStorage(
            @Value("${app.upload.cloudinary.url}") String cloudinaryUrl,
            @Value("${app.upload.cloudinary.folder}") String folder) {
        if (cloudinaryUrl == null || cloudinaryUrl.isBlank()) {
            throw new IllegalStateException(
                    "app.upload.storage=cloudinary 但沒有設定 CLOUDINARY_URL 環境變數");
        }
        // 連線字串裡含 api_secret，SDK 會自己拆解；不要把它 log 出來
        this.cloudinary = new Cloudinary(cloudinaryUrl);
        this.folder = folder;
        log.info("圖片儲存：Cloudinary，cloud={}，folder={}", cloudinary.config.cloudName, folder);
    }

    @Override
    public String store(byte[] content, String extension) {
        try {
            Map<?, ?> result = cloudinary.uploader().upload(content, ObjectUtils.asMap(
                    // 放進指定資料夾，之後每日重置才能「刪整個資料夾」
                    "folder", folder,
                    // 讓 Cloudinary 產生隨機 public_id，效果等同本機版的 UUID 檔名
                    "use_filename", false,
                    "unique_filename", true,
                    // 明確說是圖片，不讓它自動猜
                    "resource_type", "image"));

            // secure_url 是 https 網址；瀏覽器在 https 頁面載 http 圖會被擋，所以一律用 secure
            String url = (String) result.get("secure_url");
            log.debug("Cloudinary 上傳完成：{}", url);
            return url;
        } catch (IOException e) {
            // 網路層問題（連不上、逾時）
            log.error("Cloudinary 連線失敗", e);
            throw new BusinessException(ErrorCode.UPLOAD_FAILED);
        } catch (RuntimeException e) {
            // Cloudinary 回的 API 錯誤（權限不足、額度用完、key 錯）SDK 包成 RuntimeException 丟出來。
            // 訊息本身對排錯很有用，記 error 不記 stack trace
            log.error("Cloudinary 拒絕上傳：{}", e.getMessage());
            throw new BusinessException(ErrorCode.UPLOAD_FAILED);
        }
    }
}
