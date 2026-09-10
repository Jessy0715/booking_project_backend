package com.jessy.booking_project.controller;

import com.jessy.booking_project.dto.request.RoomCreateRequest;
import com.jessy.booking_project.dto.response.ApiResponse;
import com.jessy.booking_project.dto.response.PaginationResponse;
import com.jessy.booking_project.dto.response.RoomResponse;
import com.jessy.booking_project.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** 場地端點。只管 HTTP：路徑、參數、回應包裝。狀態碼與錯誤交給 GlobalExceptionHandler。 */
@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

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
