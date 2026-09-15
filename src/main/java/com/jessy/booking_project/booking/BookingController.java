package com.jessy.booking_project.booking;

import com.jessy.booking_project.booking.dto.BookingCreateRequest;
import com.jessy.booking_project.booking.dto.BookingResponse;
import com.jessy.booking_project.booking.dto.BookingReviewRequest;
import com.jessy.booking_project.common.ApiResponse;
import com.jessy.booking_project.config.OpenApiConfig;
import com.jessy.booking_project.security.AuthPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Tag(name = "Bookings 預約")
public class BookingController {

    private final BookingService bookingService;

    /** status 直接收 enum：EnumConverterConfig 負責 "pending" → PENDING，錯值自動 400。 */
    @GetMapping
    @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME)
    @Operation(
            summary = "查詢預約列表（多條件篩選）",
            description = """
                    需要登入。三個條件都可省略、可任意組合（AND）。

                    排序固定 `createdAt` 由新到舊，沒有分頁。查無資料回 `data: []`。

                    後端**不會**自動只回自己的預約，「我的預約」頁要自己帶 `userId`。
                    """)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "查詢成功（沒有 pagination 欄位）",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": true,
                                      "data": [
                                        {
                                          "id": 12,
                                          "roomId": 1,
                                          "roomTitle": "Studio A — 日光大空間",
                                          "userId": 2,
                                          "userName": "王小明",
                                          "date": "2026-09-15",
                                          "timeSlot": "morning",
                                          "reason": "商品拍攝",
                                          "status": "pending",
                                          "createdAt": "2026-09-09 10:22:00"
                                        }
                                      ]
                                    }
                                    """))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "status 不是合法值，或 userId / roomId 不是數字",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    { "success": false, "message": "參數 status 格式錯誤" }"""))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401", description = "沒帶 token 或已過期")
    })
    public ApiResponse<List<BookingResponse>> searchBookings(

            @Parameter(
                    description = "只看某個使用者的預約。值是登入回傳的 `data.id`。",
                    example = "2")
            @RequestParam(required = false) Long userId,

            @Parameter(
                    description = "只看某個狀態的預約。全小寫，送其他值會回 400。",
                    example = "pending",
                    schema = @Schema(type = "string", allowableValues = {"pending", "approved", "rejected"}))
            @RequestParam(required = false) BookingStatus status,

            @Parameter(
                    description = "只看某間棚的預約。值是場地 id。",
                    example = "1")
            @RequestParam(required = false) Long roomId) {
        return ApiResponse.ok(bookingService.search(userId, status, roomId));
    }

    @GetMapping("/{id}")
    @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME)
    @Operation(
            summary = "查詢單筆預約詳情",
description = "需要登入。回的形狀與列表中的每一筆相同。")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "查詢成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401", description = "沒帶 token 或已過期"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "預約不存在",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    { "success": false, "message": "預約不存在" }""")))
    })
    public ApiResponse<BookingResponse> getBooking(
            @Parameter(description = "預約 id", example = "12", required = true)
            @PathVariable Long id) {
        return ApiResponse.ok(bookingService.getById(id));
    }

    /** @AuthenticationPrincipal 直接拿到 filter 放進去的 AuthPrincipal，不查 DB。 */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME)
    @Operation(
            summary = "送出預約申請",
            description = """
                    需要登入。Body 沒有 `userId`，預約人身分取自 token。

                    建立後狀態一律是 `pending`。

                    先查 slots 不能取代後端檢查：查詢到送出之間可能被別人搶走，
                    **一定要處理 409**。
                    """)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "預約建立成功，狀態為 pending",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": true,
                                      "data": {
                                        "id": 13,
                                        "roomId": 1,
                                        "roomTitle": "Studio A — 日光大空間",
                                        "userId": 2,
                                        "userName": "王小明",
                                        "date": "2026-09-15",
                                        "timeSlot": "morning",
                                        "reason": "商品拍攝",
                                        "status": "pending",
                                        "createdAt": "2026-09-15 14:30:00"
                                      }
                                    }
                                    """))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "缺必填欄位，或 timeSlot 值不合法",
                    content = @Content(mediaType = "application/json",
                            examples = {
                                    @ExampleObject(name = "缺必填", value = """
                                            { "success": false, "message": "roomId、date、timeSlot 為必填" }"""),
                                    @ExampleObject(name = "timeSlot 錯", value = """
                                            { "success": false, "message": "timeSlot 必須是 morning / afternoon / night" }"""),
                                    @ExampleObject(name = "日期格式錯", value = """
                                            { "success": false, "message": "請求內容格式錯誤" }""")})),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401", description = "沒帶 token 或已過期"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "場地不存在",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    { "success": false, "message": "場地不存在" }"""))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "時段已被佔用。兩種訊息代表不同情境，直接顯示 message 即可。",
                    content = @Content(mediaType = "application/json",
                            examples = {
                                    @ExampleObject(name = "已被核准", value = """
                                            { "success": false, "message": "該時段已核准，無法重複預約" }"""),
                                    @ExampleObject(name = "已有人申請中", value = """
                                            { "success": false, "message": "該時段已有人申請中，請選擇其他時間" }""")}))
    })
    public ApiResponse<BookingResponse> createBooking(@Valid @RequestBody BookingCreateRequest request,
                                                      @AuthenticationPrincipal AuthPrincipal me) {
        return ApiResponse.ok(bookingService.create(request, me));
    }

    /**
     * SecurityConfig 已限 PATCH /api/bookings/** 為 ADMIN。
     *
     * <p>me 不是用來判斷權限的（那已經在 SecurityConfig 做掉了），
     * 是為了稽核：要能回答「這筆是誰審的」。
     */
    @PatchMapping("/{id}")
    @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME)
    @Operation(
            summary = "審核預約：核准或退回（admin）",
            description = """
                    需要 admin 權限。

                    **只有 `pending` 能被審核**，已審核過的回 400。

                    改成 `rejected` 後該時段釋出，可被重新預約。
                    """)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "審核成功，回更新後的預約"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "status 值不合法，或已審核過",
                    content = @Content(mediaType = "application/json",
                            examples = {
                                    @ExampleObject(name = "已審核過", value = """
                                            { "success": false, "message": "此預約已審核完畢，無法再次變更" }"""),
                                    @ExampleObject(name = "status 錯", value = """
                                            { "success": false, "message": "status 只能是 approved 或 rejected" }""")})),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401", description = "沒帶 token 或已過期"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403", description = "不是 admin"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "預約不存在",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    { "success": false, "message": "預約不存在" }""")))
    })
    public ApiResponse<BookingResponse> reviewBooking(
            @Parameter(description = "預約 id", example = "12", required = true)
            @PathVariable Long id,
            @Valid @RequestBody BookingReviewRequest request,
            @AuthenticationPrincipal AuthPrincipal me) {
        return ApiResponse.ok(bookingService.review(id, request, me));
    }

    @DeleteMapping("/{id}")
    @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME)
    @Operation(
            summary = "取消預約（本人或 admin）",
            description = """
                    需要登入。只有本人或 admin 能取消，且只有 `pending` 能取消。

                    取消後該時段釋出，這筆預約不再出現在列表。回 `message` 不回 `data`。
                    """)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "取消成功（回 message 不回 data）",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    { "success": true, "message": "預約已取消" }"""))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "這筆預約不是 pending，不能取消",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    { "success": false, "message": "只能取消審核中 (pending) 的預約" }"""))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401", description = "沒帶 token 或已過期"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "想取消別人的預約，但自己不是 admin",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    { "success": false, "message": "不能取消別人的預約" }"""))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "預約不存在",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    { "success": false, "message": "預約不存在" }""")))
    })
    public ApiResponse<Void> cancelBooking(
            @Parameter(description = "預約 id", example = "12", required = true)
            @PathVariable Long id,
            @AuthenticationPrincipal AuthPrincipal me) {
        bookingService.cancel(id, me);
        return ApiResponse.okMessage("預約已取消");
    }
}
