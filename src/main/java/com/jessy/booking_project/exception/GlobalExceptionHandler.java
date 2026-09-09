package com.jessy.booking_project.exception;

import com.jessy.booking_project.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Objects;

/**
 * 全域例外處理。所有 Controller 丟出的例外都在這裡轉成契約要的錯誤格式。
 *
 * <p>Controller 因此可以完全不寫 try/catch，也不用知道任何狀態碼。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 找不到資源 → 404。訊息用例外自己帶的中文，前端會直接顯示。 */
    @ExceptionHandler(RoomNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleRoomNotFound(RoomNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.fail(e.getMessage()));
    }

    /** Bean Validation 失敗 → 400。取第一個欄位錯誤當訊息，契約要的是單一句中文。 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse("參數錯誤");
        return ResponseEntity.badRequest().body(ApiResponse.fail(message));
    }

    /** 路徑或查詢參數型別錯（例如 /api/rooms/abc）→ 400。是呼叫端的錯，不該回 500。 */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return ResponseEntity.badRequest().body(ApiResponse.fail("參數 " + e.getName() + " 格式錯誤"));
    }

    /** request body 不是合法 JSON → 400。階段 6 開始有 POST 之後會用到。 */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnreadableBody(HttpMessageNotReadableException e) {
        log.debug("request body 解析失敗", e);
        return ResponseEntity.badRequest().body(ApiResponse.fail("請求內容格式錯誤"));
    }

    /** 路由不存在 → 404。Boot 3.2 之後未匹配的路徑丟的是 NoResourceFoundException。 */
    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    public ResponseEntity<ApiResponse<Void>> handleNoRoute() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.fail("找不到此路由"));
    }

    /** 兜底 → 500。訊息固定，不把內部細節洩漏給前端；細節寫進 log 給自己看。 */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception e) {
        log.error("未預期的例外", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail("伺服器內部錯誤"));
    }
}
