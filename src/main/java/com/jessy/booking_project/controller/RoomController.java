package com.jessy.booking_project.controller;

import com.jessy.booking_project.exception.RoomNotFoundException;
import com.jessy.booking_project.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** 場地端點。只管 HTTP：路徑、狀態碼、回應包裝。 */
@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getRoom(@PathVariable Long id) {
        // Map 是暫時的。階段 5 抽成 ApiResponse<T>。
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", roomService.getById(id)
        ));
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
