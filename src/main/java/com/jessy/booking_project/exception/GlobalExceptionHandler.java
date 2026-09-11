package com.jessy.booking_project.exception;

import com.jessy.booking_project.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
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

    /** 找不到資源 → 404。抓父類，Room / Booking / 未來的任何 NotFound 都走這裡。 */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(ResourceNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.fail(e.getMessage()));
    }

    /** 預約時段衝突 → 409。 */
    @ExceptionHandler(BookingConflictException.class)
    public ResponseEntity<ApiResponse<Void>> handleBookingConflict(BookingConflictException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.fail(e.getMessage()));
    }

    /** 預約狀態不允許此操作 → 400。 */
    @ExceptionHandler(BookingStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleBookingState(BookingStateException e) {
        return ResponseEntity.badRequest().body(ApiResponse.fail(e.getMessage()));
    }

    /**
     * 撞到資料庫唯一索引 → 409。
     *
     * <p>這是併發的最後防線：兩個請求同時通過 Service 的檢查，只有一個能 INSERT 成功，
     * 另一個在這裡被接住。此時分不出對方是 pending 還是 approved，用通用訊息。
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrity(DataIntegrityViolationException e) {
        log.warn("資料完整性衝突：{}", e.getMostSpecificCause().getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.fail(BookingConflictException.PENDING_MESSAGE));
    }

    /** Service 內丟的 AccessDeniedException（本人檢查）→ 403。Filter 層的 403 走 RestAccessDeniedHandler。 */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.fail(e.getMessage()));
    }

    /** 帳密錯誤 → 401。 */
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidCredentials(InvalidCredentialsException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.fail(e.getMessage()));
    }

    /** 帳號重複 → 409。 */
    @ExceptionHandler(DuplicateAccountException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateAccount(DuplicateAccountException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.fail(e.getMessage()));
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

    /** 必填的 query param 沒帶（例如 slots 少了 date）→ 400。訊息格式對齊契約「date 為必填」。 */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingParam(MissingServletRequestParameterException e) {
        return ResponseEntity.badRequest().body(ApiResponse.fail(e.getParameterName() + " 為必填"));
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
