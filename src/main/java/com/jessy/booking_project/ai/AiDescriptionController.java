package com.jessy.booking_project.ai;

import com.jessy.booking_project.ai.dto.RoomDescriptionRequest;
import com.jessy.booking_project.ai.dto.RoomDescriptionResponse;
import com.jessy.booking_project.common.ApiResponse;
import com.jessy.booking_project.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI 輔助端點。
 *
 * <p>路徑放 /api/ai 而不是 /api/rooms：這支不動任何場地資料，
 * 它是「幫你想一段文字」的工具，場地當下甚至還不存在。
 *
 * <p>限 admin（SecurityConfig）—— 每次呼叫都是真的花錢。
 */
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Tag(name = "AI 輔助")
public class AiDescriptionController {

    private final AiDescriptionService aiDescriptionService;

    /** 回 200 不是 201：沒有建立任何資源，只是回一段建議文字。 */
    @PostMapping("/room-description")
    @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME)
    @Operation(
            summary = "依名稱與照片生成場地說明（admin）",
            description = """
                    讓 AI 看照片與場地名稱，寫一段約 60 字的說明。

                    **結果不會自動儲存**，只是回一段建議文字，讓使用者編輯後隨場地表單一起送出。

                    名稱與照片缺一不可，缺任一個回 400 且不產生費用。
                    `roomImg` 必須是 `POST /api/uploads/images` 回傳的網址。

                    這支通常要 2～10 秒，前端請顯示 loading 並把逾時設久一點（建議 30 秒以上）。
                    每次結果不同，重按會得到不一樣的文字。
                    """)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "生成成功",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": true,
                                      "data": {
                                        "description": "水泥原牆搭配大面窗光，空間開闊，適合工業風人像與服裝形象拍攝。"
                                      }
                                    }
                                    """))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "名稱或照片缺其中一個（不產生 AI 費用）",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    { "success": false, "message": "請先填寫場地名稱並上傳照片" }"""))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401", description = "沒帶 token 或已過期"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403", description = "不是 admin"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "照片讀不到（網址不是本站上傳的，或檔案已刪除）",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    { "success": false, "message": "讀取圖片失敗" }"""))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "503",
                    description = "AI 服務暫時無法使用。暫時性錯誤，提示稍後再試並保留表單內容。",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    { "success": false, "message": "AI 生成失敗，請稍後再試" }""")))
    })
    public ApiResponse<RoomDescriptionResponse> generateRoomDescription(
            @Valid @RequestBody RoomDescriptionRequest request) {

        String description = aiDescriptionService.generate(request);
        return ApiResponse.ok(new RoomDescriptionResponse(description));
    }
}
