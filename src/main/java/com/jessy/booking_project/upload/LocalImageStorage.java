package com.jessy.booking_project.upload;

import com.jessy.booking_project.common.BusinessException;
import com.jessy.booking_project.common.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * 存在本機硬碟。檔名一律換成 UUID：
 * <ul>
 *   <li>不會撞名（兩個人都上傳 photo.jpg）</li>
 *   <li>擋掉路徑攻擊（原檔名可能是 ../../etc/passwd）</li>
 *   <li>不洩漏使用者的原始檔名</li>
 * </ul>
 *
 * <p>這個 class 不做任何驗證 —— 那是 UploadService 的事。
 *
 * <p>只在 app.upload.storage=local（或沒設）時啟用；設成 cloudinary 時這個 bean 不會被建立。
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "app.upload.storage", havingValue = "local", matchIfMissing = true)
public class LocalImageStorage implements ImageStorage {

    private final Path uploadDir;
    private final String baseUrl;

    public LocalImageStorage(
            @Value("${app.upload.dir}") String uploadDir,
            @Value("${app.upload.base-url}") String baseUrl) throws IOException {
        this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        Files.createDirectories(this.uploadDir);
        log.info("圖片上傳資料夾：{}", this.uploadDir);
    }

    @Override
    public String store(byte[] content, String extension) {
        String filename = UUID.randomUUID() + "." + extension;
        Path target = uploadDir.resolve(filename);

        try {
            Files.write(target, content);
        } catch (IOException e) {
            log.error("圖片寫入失敗：{}", target, e);
            throw new BusinessException(ErrorCode.UPLOAD_FAILED);
        }

        return baseUrl + "/" + filename;
    }
}
