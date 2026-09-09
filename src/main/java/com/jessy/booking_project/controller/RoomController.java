package com.jessy.booking_project.controller;

import com.jessy.booking_project.dto.response.PaginationResponse;
import com.jessy.booking_project.dto.response.RoomResponse;
import com.jessy.booking_project.exception.RoomNotFoundException;
import com.jessy.booking_project.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** 場地端點。只管 HTTP：路徑、參數、狀態碼、回應包裝。 */
@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> listRooms(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int pageSize) {

        Page<RoomResponse> result = roomService.search(keyword, page, pageSize);

        // Map 是暫時的。階段 5 抽成 ApiResponse<T>。
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", result.getContent(),
                "pagination", toPagination(result, page, pageSize)
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getRoom(@PathVariable Long id) {
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", roomService.getById(id)
        ));
    }

    private PaginationResponse toPagination(Page<RoomResponse> result, int page, int pageSize) {
        // page/pageSize 回使用者問的原值（1 起算）；總數交給 Page 算
        return new PaginationResponse(
                page,
                pageSize,
                result.getTotalElements(),
                result.getTotalPages());
    }

    /** 只作用於這個 Controller。階段 6 搬到 @RestControllerAdvice 變全域。 */
    @ExceptionHandler(RoomNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleRoomNotFound(RoomNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "success", false,
                "message", e.getMessage()
        ));
    }
}
