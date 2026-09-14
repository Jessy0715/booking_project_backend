package com.jessy.booking_project.upload;

import com.jessy.booking_project.common.ApiResponse;
import com.jessy.booking_project.upload.dto.UploadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 圖片上傳。獨立於場地 API：前端先上傳拿 URL，再把 URL 塞進 roomImg 送場地表單。
 *
 * <p>前端送 multipart/form-data，欄位名叫 {@code file}。
 * 大小超過 application.properties 的上限時，Spring 在進這裡之前就會擋下。
 */
@RestController
@RequestMapping("/api/uploads")
@RequiredArgsConstructor
public class UploadController {

    private final UploadService uploadService;

    /** 建立了一個新資源（檔案），回 201。 */
    @PostMapping(path = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<UploadResponse> uploadImage(@RequestPart("file") MultipartFile file) {
        String url = uploadService.upload(file);
        return ApiResponse.ok(new UploadResponse(url));
    }
}
