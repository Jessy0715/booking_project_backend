package com.jessy.booking_project.upload;

import com.jessy.booking_project.common.BusinessException;
import com.jessy.booking_project.common.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * 存在本機硬碟。檔名一律換成 UUID：
 * <ul>
 *   <li>不會撞名（兩個人都上傳 photo.jpg）</li>
 *   <li>擋掉路徑攻擊（原檔名可能是 ../../etc/passwd）</li>
 *   <li>不洩漏使用者的原始檔名</li>
 * </ul>
 */
@Slf4j
@Component
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
    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.UPLOAD_EMPTY);
        }

        ImageType type = detectType(file);
        String filename = UUID.randomUUID() + "." + type.extension();
        Path target = uploadDir.resolve(filename);

        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("圖片寫入失敗：{}", target, e);
            throw new BusinessException(ErrorCode.UPLOAD_FAILED);
        }

        return baseUrl + "/" + filename;
    }

    /**
     * 判斷檔案真正的格式。
     *
     * <p>這是安全邊界：副檔名和 Content-Type 都是前端說的，可以造假。
     * 把 virus.exe 改名成 photo.jpg 上傳，副檔名檢查會放行。
     */
    private ImageType detectType(MultipartFile file) {
        // 只讀檔案開頭 12 bytes，不把整張圖載進記憶體。
        // 12 是三種格式中最長的簽名（WEBP）需要的長度。
        byte[] head;
        try (InputStream in = file.getInputStream()) {
            head = in.readNBytes(12);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.UPLOAD_FAILED);
        }

        // 每種圖片格式的檔案開頭都有固定的「簽名」（magic bytes），
        // 這是檔案格式規範定的，改副檔名改不掉。
        if (startsWith(head, JPEG_MAGIC)) {
            return ImageType.JPEG;
        }
        if (startsWith(head, PNG_MAGIC)) {
            return ImageType.PNG;
        }
        // WEBP 的簽名分兩段：第 0–3 byte 是 "RIFF"，第 4–7 是檔案大小（每張圖都不同，跳過），
        // 第 8–11 才是 "WEBP"。所以要分開比兩段。
        if (startsWith(head, RIFF_MAGIC) && matchesAt(head, 8, WEBP_MAGIC)) {
            return ImageType.WEBP;
        }

        throw new BusinessException(ErrorCode.UPLOAD_TYPE_NOT_ALLOWED);
    }

    // Java 的 byte 是有號的（-128 ~ 127），0xFF 超過 127，所以要強制轉型 (byte) 才放得進去。
    private static final byte[] JPEG_MAGIC = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    private static final byte[] PNG_MAGIC = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A};
    private static final byte[] RIFF_MAGIC = {'R', 'I', 'F', 'F'};
    private static final byte[] WEBP_MAGIC = {'W', 'E', 'B', 'P'};

    /** data 是否以 prefix 開頭。 */
    private static boolean startsWith(byte[] data, byte[] prefix) {
        return matchesAt(data, 0, prefix);
    }

    /** data 從第 offset 個 byte 起，是否跟 pattern 完全相同。長度不夠直接算不符合。 */
    private static boolean matchesAt(byte[] data, int offset, byte[] pattern) {
        if (data.length < offset + pattern.length) {
            return false;
        }
        for (int i = 0; i < pattern.length; i++) {
            if (data[offset + i] != pattern[i]) {
                return false;
            }
        }
        return true;
    }

    /** 允許的格式與對應的副檔名。 */
    enum ImageType {
        JPEG("jpg"), PNG("png"), WEBP("webp");

        private final String extension;

        ImageType(String extension) {
            this.extension = extension;
        }

        String extension() {
            return extension;
        }
    }
}
