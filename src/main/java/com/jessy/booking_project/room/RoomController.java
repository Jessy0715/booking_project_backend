package com.jessy.booking_project.room;

import com.jessy.booking_project.booking.BookingService;
import com.jessy.booking_project.booking.dto.SlotAvailabilityResponse;
import com.jessy.booking_project.common.ApiResponse;
import com.jessy.booking_project.common.PaginationResponse;
import com.jessy.booking_project.config.OpenApiConfig;
import com.jessy.booking_project.room.dto.RoomCreateRequest;
import com.jessy.booking_project.room.dto.RoomOptionResponse;
import com.jessy.booking_project.room.dto.RoomResponse;
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
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/** 場地端點。只管 HTTP：路徑、參數、回應包裝。狀態碼與錯誤交給 GlobalExceptionHandler。 */
@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
@Tag(name = "Rooms 場地")
public class RoomController {

    private final RoomService roomService;
    private final BookingService bookingService;

/*
* 統一原則為[Controller動詞+名詞、Service用同一動詞】
* */

    @GetMapping
    @Operation(
            summary = "查詢場地列表（分頁 + 關鍵字）",
            description = """
                    不用登入。唯一會回 `pagination` 的端點。

                    排序固定 id 由小到大。查無結果回 `data: []` 與 `total: 0`，不是 404。
                    """)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "查詢成功",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": true,
                                      "data": [
                                        {
                                          "id": 1,
                                          "roomImg": "https://images.unsplash.com/photo-1497366216548-37526070297c",
                                          "title": "Studio A — 日光大空間",
                                          "desc": "一樓主棚，採光頂天窗，最多可容納 10 人拍攝團隊。",
                                          "floor": "1F",
                                          "area": 80,
                                          "capacity": 10,
                                          "facilities": ["閃燈組", "柔光箱", "背景架", "造型椅"],
                                          "price": { "morning": "2000", "afternoon": "3000", "night": "4000" },
                                          "createdAt": "2026-04-16 20:30:00"
                                        }
                                      ],
                                      "pagination": { "page": 1, "pageSize": 5, "total": 10, "totalPages": 2 }
                                    }
                                    """)))
    })
    public ApiResponse<List<RoomResponse>> searchRooms(

            @Parameter(
                    description = "關鍵字，只對場地名稱做模糊比對，不分大小寫。空白或省略視為不篩選。",
                    example = "Studio A")
            @RequestParam(required = false) String keyword,

            @Parameter(
                    description = "第幾頁，從 1 開始。超出總頁數回空陣列，不是錯誤。",
                    example = "1")
            @RequestParam(defaultValue = "1") int page,

            @Parameter(
                    description = "每頁幾筆，上限 100（超過會被夾到 100）。實際生效的值見 `pagination.pageSize`。",
                    example = "5")
            @RequestParam(defaultValue = "5") int pageSize) {

        Page<RoomResponse> result = roomService.search(keyword, page, pageSize);
        return ApiResponse.ok(result.getContent(), toPagination(result, page));
    }

    /**
     * 放在 /{id} 前面：Spring 比對路徑時，字面的 "/options" 比 "/{id}" 精確，
     * 所以其實順序不影響結果，但放前面讓讀的人一眼看出「這不是 id」。
     */
    @GetMapping("/options")
    @Operation(
            summary = "查詢全部場地的下拉選項",
            description = """
                    不用登入。回全部場地，只有 `id` 與 `title`，不分頁。

                    給表單的下拉選單用。要完整資料請用 `GET /api/rooms`。
                    """)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "查詢成功。沒有場地時回空陣列。",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": true,
                                      "data": [
                                        { "id": 1, "title": "Studio A — 日光大空間" },
                                        { "id": 2, "title": "Studio B — 純白無縫棚" }
                                      ]
                                    }
                                    """)))
    })
    public ApiResponse<List<RoomOptionResponse>> getRoomOptions() {
        return ApiResponse.ok(roomService.listOptions());
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "查詢單一場地詳情",
            description = "不用登入。回的形狀與列表中的每一筆相同。")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "查詢成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "id 不是數字",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    { "success": false, "message": "參數 id 格式錯誤" }"""))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "場地不存在",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    { "success": false, "message": "場地不存在" }""")))
    })
    public ApiResponse<RoomResponse> getRoom(
            @Parameter(description = "場地 id", example = "1", required = true)
            @PathVariable Long id) {
        return ApiResponse.ok(roomService.getById(id));
    }

    /**
     * 路徑屬於 rooms，資料來自 bookings —— 所以掛在這裡但呼叫 BookingService。
     * date 沒帶 → MissingServletRequestParameterException → 400「date 為必填」。
     */
    @GetMapping("/{id}/slots")
    @Operation(
            summary = "查詢某天三個時段的可用狀態",
            description = """
                    預約日曆用，不用登入。回 `{ morning, afternoon, night }`，
                    值為 `available` / `pending` / `approved`。

                    被退回（rejected）的預約不佔位，該時段會是 `available`。
                    """)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "查詢成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "缺少 date，或格式不是 yyyy-MM-dd",
                    content = @Content(mediaType = "application/json",
                            examples = {
                                    @ExampleObject(name = "沒帶 date", value = """
                                            { "success": false, "message": "date 為必填" }"""),
                                    @ExampleObject(name = "格式錯誤", value = """
                                            { "success": false, "message": "參數 date 格式錯誤" }""")})),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "場地不存在",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    { "success": false, "message": "場地不存在" }""")))
    })
    public ApiResponse<SlotAvailabilityResponse> getSlots(
            @Parameter(description = "場地 id", example = "1", required = true)
            @PathVariable Long id,

            @Parameter(
                    description = "要查的日期，格式 yyyy-MM-dd。必填。",
                    example = "2026-09-15",
                    required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ApiResponse.ok(bookingService.getSlotAvailability(id, date));
    }

    /** 契約要 201 Created。@ResponseStatus 比包一層 ResponseEntity 乾淨。 */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME)
    @Operation(
            summary = "新增場地（admin）",
            description = """
                    只有 `title` 必填，其餘省略時補預設值（文字 `""`、數字 `0`、陣列 `[]`）。

                    有照片時先呼叫 `POST /api/uploads/images` 拿 url，再填進 `roomImg`。
                    """)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201", description = "建立成功，回新建立的場地"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "title 沒填，或 JSON 格式錯誤",
                    content = @Content(mediaType = "application/json",
                            examples = {
                                    @ExampleObject(name = "缺 title", value = """
                                            { "success": false, "message": "title 為必填" }"""),
                                    @ExampleObject(name = "JSON 壞掉", value = """
                                            { "success": false, "message": "請求內容格式錯誤" }""")})),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401", description = "沒帶 token 或已過期"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "不是 admin",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    { "success": false, "message": "權限不足" }""")))
    })
    public ApiResponse<RoomResponse> createRoom(@Valid @RequestBody RoomCreateRequest request) {
        return ApiResponse.ok(roomService.create(request));
    }

    @PutMapping("/{id}")
    @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME)
    @Operation(
            summary = "修改場地（admin）",
            description = """
                    Body 與新增相同。

                    **這是全量覆蓋**：沒帶的欄位會被補成預設值，不是保留原值。
                    請先 GET 拿到完整資料，改掉要改的再整包送回。

                    `roomImg` 換成新網址時，舊照片由後端自動刪除。
                    """)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "修改成功，回更新後的場地"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "title 沒填，或 JSON 格式錯誤",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    { "success": false, "message": "title 為必填" }"""))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401", description = "沒帶 token 或已過期"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403", description = "不是 admin"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "場地不存在",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    { "success": false, "message": "場地不存在" }""")))
    })
    public ApiResponse<RoomResponse> updateRoom(
            @Parameter(description = "場地 id", example = "1", required = true)
            @PathVariable Long id,
            @Valid @RequestBody RoomCreateRequest request) {
        return ApiResponse.ok(roomService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME)
    @Operation(
            summary = "刪除單一場地（admin）",
            description = """
                    回 `message` 不回 `data`。該場地的照片由後端自動清除。

                    場地若還有預約紀錄會因外鍵限制失敗（500）。
                    """)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "刪除成功（回 message 不回 data）",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    { "success": true, "message": "場地已刪除" }"""))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401", description = "沒帶 token 或已過期"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403", description = "不是 admin"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "場地不存在",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    { "success": false, "message": "場地不存在" }""")))
    })
    public ApiResponse<Void> deleteRoom(
            @Parameter(description = "場地 id", example = "1", required = true)
            @PathVariable Long id) {
        roomService.delete(id);
        return ApiResponse.okMessage("場地已刪除");
    }

    /**
     * 批次刪除：DELETE /api/rooms?ids=1,2,3
     *
     * <p>全有全無 —— 任何一個 id 不存在就整批不刪，回 404「場地不存在」。
     *
     * <p>路徑跟單筆的 /{id} 不衝突：這支沒有路徑變數，Spring 靠這點分辨。
     * ids 是必填，沒帶會走 MissingServletRequestParameterException → 400「ids 為必填」。
     */
    @DeleteMapping
    @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME)
    @Operation(
            summary = "批次刪除場地（admin）",
            description = """
                    **全有全無**：只要有一個 id 不存在，整批都不刪，回 404。
                    失敗就是完全沒動，可以安心重試。

                    重複的 id 會自動去重。回 `message` 不回 `data`，訊息帶實際刪除筆數。
                    """)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "全部刪除成功（回 message 不回 data）",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    { "success": true, "message": "已刪除 3 筆場地" }"""))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "沒帶 ids，或 ids 裡有非數字",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    { "success": false, "message": "ids 為必填" }"""))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401", description = "沒帶 token 或已過期"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403", description = "不是 admin"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "其中任何一個 id 不存在，整批未刪除",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    { "success": false, "message": "場地不存在" }""")))
    })
    public ApiResponse<Void> deleteRooms(
            @Parameter(
                    description = "場地 id 清單，逗號分隔（例如 `1,2,3`）。必填。",
                    example = "1,2,3",
                    required = true,
                    schema = @Schema(type = "string"))
            @RequestParam List<Long> ids) {
        int deleted = roomService.deleteAll(ids);
        return ApiResponse.okMessage("已刪除 " + deleted + " 筆場地");
    }

    /** pageSize 用 result.getSize()：那是 Service 夾過之後「實際生效」的值，不是使用者亂傳的原值。 */
    private PaginationResponse toPagination(Page<RoomResponse> result, int page) {
        return new PaginationResponse(
                page,
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages());
    }
}
