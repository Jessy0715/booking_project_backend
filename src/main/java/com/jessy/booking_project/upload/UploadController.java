package com.jessy.booking_project.upload;

import com.jessy.booking_project.common.ApiResponse;
import com.jessy.booking_project.config.OpenApiConfig;
import com.jessy.booking_project.upload.dto.UploadResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Uploads 圖片上傳")
public class UploadController {

    private final UploadService uploadService;

    /** 建立了一個新資源（檔案），回 201。 */
    @PostMapping(path = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME)
    @Operation(
            summary = "上傳場地照片（admin）",
            description = """
                    上傳圖片，回可直接使用的公開網址。拿到後填進場地表單的 `roomImg`。

                    **這支是 multipart/form-data，欄位名固定是 `file`。**
                    送 JSON 會回 415。前端用 FormData 時不要自己設 Content-Type，
                    瀏覽器要自動加 boundary。

                    限 jpg / png / webp、5 MB。格式是看**檔案內容**判斷，改副檔名沒用。

                    後端會自動縮圖（最長邊 1280px、轉 JPEG）並改成隨機檔名，EXIF 會被移除。

                    圖片的刪除由後端在換圖或刪場地時自動處理，前端不需要呼叫刪除 API。
                    """)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "上傳成功，回圖片網址",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": true,
                                      "data": {
                                        "url": "https://res.cloudinary.com/demo/image/upload/v1699999999/booking/rooms/abc123.jpg"
                                      }
                                    }
                                    """))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "沒選檔案，或不是合法的 jpg / png / webp",
                    content = @Content(mediaType = "application/json",
                            examples = {
                                    @ExampleObject(name = "沒選檔", value = """
                                            { "success": false, "message": "請選擇要上傳的圖片" }"""),
                                    @ExampleObject(name = "格式不符", value = """
                                            { "success": false, "message": "只支援 jpg / png / webp 格式的圖片" }""")})),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401", description = "沒帶 token 或已過期"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403", description = "不是 admin"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "413",
                    description = "檔案超過 5 MB",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    { "success": false, "message": "圖片大小不能超過 5 MB" }"""))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "415",
                    description = "Content-Type 不是 multipart/form-data",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    { "success": false, "message": "請求格式不支援，上傳檔案請用 multipart/form-data" }"""))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "後端存檔失敗，可重試",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    { "success": false, "message": "圖片儲存失敗" }""")))
    })
    public ApiResponse<UploadResponse> uploadImage(
            @Parameter(
                    description = "要上傳的圖片檔。欄位名必須是 `file`。限 jpg / png / webp，5 MB 以內。",
                    required = true,
                    schema = @Schema(type = "string", format = "binary"))
            @RequestPart("file") MultipartFile file) {
        String url = uploadService.upload(file);
        return ApiResponse.ok(new UploadResponse(url));
    }
}
