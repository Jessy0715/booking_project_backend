package com.jessy.booking_project.ai;

import com.jessy.booking_project.ai.dto.RoomDescriptionRequest;
import com.jessy.booking_project.ai.dto.RoomDescriptionResponse;
import com.jessy.booking_project.common.ApiResponse;
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
public class AiDescriptionController {

    private final AiDescriptionService aiDescriptionService;

    /** 回 200 不是 201：沒有建立任何資源，只是回一段建議文字。 */
    @PostMapping("/room-description")
    public ApiResponse<RoomDescriptionResponse> generateRoomDescription(
            @Valid @RequestBody RoomDescriptionRequest request) {

        String description = aiDescriptionService.generate(request);
        return ApiResponse.ok(new RoomDescriptionResponse(description));
    }
}
