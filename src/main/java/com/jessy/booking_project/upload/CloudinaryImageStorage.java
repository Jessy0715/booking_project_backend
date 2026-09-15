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

    @Override
    public boolean owns(String url) {
        // 認自己的 cloud name：別人家的 Cloudinary 網址不歸我管
        return url != null && url.contains("res.cloudinary.com/" + cloudinary.config.cloudName + "/");
    }

    @Override
    public void delete(String url) {
        if (!owns(url)) {
            return;
        }

        String publicId = toPublicId(url);
        if (publicId == null) {
            log.warn("無法從網址解析 public_id，略過刪除：{}", url);
            return;
        }

        try {
            Map<?, ?> result = cloudinary.uploader().destroy(publicId, ObjectUtils.asMap(
                    "resource_type", "image",
                    // Cloudinary 的 CDN 會快取，不清的話刪掉的圖還會被看到一陣子
                    "invalidate", true));
            log.debug("刪除 Cloudinary 圖片 {}：{}", publicId, result.get("result"));
        } catch (IOException | RuntimeException e) {
            // 同本機版：刪不掉不該讓使用者的操作失敗
            log.warn("刪除 Cloudinary 圖片失敗：{}，{}", publicId, e.getMessage());
        }
    }

    /**
     * 從圖片網址反推 public_id —— 刪除 API 認的是 public_id，不是網址。
     *
     * <pre>
     * https://res.cloudinary.com/demo/image/upload/v1699999999/booking/rooms/abc123.jpg
     *                                            └─ 版本號，要拿掉
     *                                                        └── public_id ──┘ └─ 副檔名，要拿掉
     * → booking/rooms/abc123
     * </pre>
     */
    private String toPublicId(String url) {
        int uploadAt = url.indexOf(UPLOAD_SEGMENT);
        if (uploadAt < 0) {
            return null;
        }
        String path = url.substring(uploadAt + UPLOAD_SEGMENT.length());

        // 版本號長得像 v1699999999，後面接斜線。不是每個網址都有
        int slash = path.indexOf('/');
        if (slash > 1 && path.charAt(0) == 'v' && path.substring(1, slash).chars().allMatch(Character::isDigit)) {
            path = path.substring(slash + 1);
        }

        // 去副檔名。只看最後一段，資料夾名稱裡的點不能被誤砍
        int lastSlash = path.lastIndexOf('/');
        int lastDot = path.lastIndexOf('.');
        if (lastDot > lastSlash) {
            path = path.substring(0, lastDot);
        }

        return path.isBlank() ? null : path;
    }

    private static final String UPLOAD_SEGMENT = "/image/upload/";
}
