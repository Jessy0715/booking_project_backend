package com.jessy.booking_project.upload;

import com.jessy.booking_project.common.BusinessException;
import com.jessy.booking_project.common.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;

/**
 * 上傳流程的規則都在這裡：驗格式 → 縮圖 → 交給 storage 存。
 *
 * <p>storage 只做 I/O，所以不管本機還是雲端，拿到的都是同一套規則處理過的 bytes。
 */
@Service
@RequiredArgsConstructor
public class UploadService {

    private final ImageStorage imageStorage;
    private final ImageResizer imageResizer;

    public String upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.UPLOAD_EMPTY);
        }

        // 一次讀完整份（上限 5 MB，可接受）；之後縮圖也需要完整內容
        byte[] content;
        try {
            content = file.getBytes();
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.UPLOAD_FAILED);
        }

        // 只看檔案開頭判斷真正格式，不信副檔名與 Content-Type
        byte[] head = Arrays.copyOf(content, Math.min(content.length, ImageType.HEADER_LENGTH));
        ImageType type = ImageType.detect(head)
                .orElseThrow(() -> new BusinessException(ErrorCode.UPLOAD_TYPE_NOT_ALLOWED));

        // 縮圖後格式可能變（PNG 進、JPEG 出），所以副檔名要用 resized 的，不是 type 的
        ImageResizer.Result resized = imageResizer.resize(content, type);

        return imageStorage.store(resized.content(), resized.type().extension());
    }
}
