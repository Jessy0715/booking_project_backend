package com.jessy.booking_project.controller;

import com.jessy.booking_project.dto.response.ApiResponse;
import com.jessy.booking_project.dto.response.PaginationResponse;
import com.jessy.booking_project.dto.response.RoomResponse;
import com.jessy.booking_project.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

    /** pageSize 用 result.getSize()：那是 Service 夾過之後「實際生效」的值，不是使用者亂傳的原值。 */
    private PaginationResponse toPagination(Page<RoomResponse> result, int page) {
        return new PaginationResponse(
                page,
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages());
    }
}
