package com.jessy.booking_project.controller;

import com.jessy.booking_project.dto.request.RoomCreateRequest;
import com.jessy.booking_project.dto.response.ApiResponse;
import com.jessy.booking_project.dto.response.PaginationResponse;
import com.jessy.booking_project.dto.response.RoomResponse;
import com.jessy.booking_project.dto.response.SlotAvailabilityResponse;
import com.jessy.booking_project.service.BookingService;
import com.jessy.booking_project.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/** 場地端點。只管 HTTP：路徑、參數、回應包裝。狀態碼與錯誤交給 GlobalExceptionHandler。 */
@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;
    private final BookingService bookingService;

    @GetMapping
    public ApiResponse<List<RoomResponse>> listRooms(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int pageSize) {

        Page<RoomResponse> result = roomService.search(keyword, page, pageSize);
        return ApiResponse.ok(result.getContent(), toPagination(result, page));
    }

    @GetMapping("/{id}")
    public ApiResponse<RoomResponse> getRoom(@PathVariable Long id) {
        return ApiResponse.ok(roomService.getById(id));
    }

    /**
     * 路徑屬於 rooms，資料來自 bookings —— 所以掛在這裡但呼叫 BookingService。
     * date 沒帶 → MissingServletRequestParameterException → 400「date 為必填」。
     */
    @GetMapping("/{id}/slots")
    public ApiResponse<SlotAvailabilityResponse> getSlots(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ApiResponse.ok(bookingService.getSlotAvailability(id, date));
    }

    /** 契約要 201 Created。@ResponseStatus 比包一層 ResponseEntity 乾淨。 */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<RoomResponse> createRoom(@Valid @RequestBody RoomCreateRequest request) {
        return ApiResponse.ok(roomService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<RoomResponse> reviseRoom( @PathVariable Long id, @Valid @RequestBody RoomCreateRequest request) {
        return ApiResponse.ok(roomService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteRoom(@PathVariable Long id) {
        roomService.delete(id);
        return ApiResponse.okMessage("場地已刪除");
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
