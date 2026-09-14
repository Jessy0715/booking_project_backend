package com.jessy.booking_project.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Objects;

/**
 * 全域例外處理。所有 Controller 丟出的例外都在這裡轉成契約要的錯誤格式。
 *
 * <p>業務錯誤只有一個 handler：狀態碼與訊息由 ErrorCode 決定。
 * 其餘是 Spring 框架自己丟的例外，各自對應一個 handler。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 所有業務錯誤（404 / 400 / 401 / 403 / 409）都走這一個。 */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException e) {
        return fail(e.getCode());
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

    /** 必填的 query param 沒帶（例如 slots 少了 date）→ 400。 */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingParam(MissingServletRequestParameterException e) {
        return ResponseEntity.badRequest().body(ApiResponse.fail(e.getParameterName() + " 為必填"));
    }

    /** 路徑或查詢參數型別錯（例如 /api/rooms/abc）→ 400。 */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return ResponseEntity.badRequest().body(ApiResponse.fail("參數 " + e.getName() + " 格式錯誤"));
    }

    /** request body 不是合法 JSON → 400。 */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnreadableBody(HttpMessageNotReadableException e) {
        log.debug("request body 解析失敗", e);
        return fail(ErrorCode.UNREADABLE_BODY);
    }

    /** 上傳的檔案超過 spring.servlet.multipart.max-file-size → 413。Spring 在進 Controller 前就丟。 */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleUploadTooLarge() {
        return fail(ErrorCode.UPLOAD_TOO_LARGE);
    }

    /** multipart 請求裡沒有 file 這個欄位 → 400。 */
    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingPart() {
        return fail(ErrorCode.UPLOAD_EMPTY);
    }

    /**
     * 撞到資料庫唯一索引 → 409。併發的最後防線：
     * 兩個請求同時通過 Service 檢查，只有一個 INSERT 成功，另一個在這裡被接住。
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrity(DataIntegrityViolationException e) {
        log.warn("資料完整性衝突：{}", e.getMostSpecificCause().getMessage());
        return fail(ErrorCode.BOOKING_SLOT_PENDING);
    }

    /** @PreAuthorize 之類在方法層丟的 403。Filter 層的 403 走 RestAccessDeniedHandler。 */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException e) {
        return ResponseEntity.status(403).body(ApiResponse.fail("權限不足"));
    }

    /** 路由不存在 → 404。 */
    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    public ResponseEntity<ApiResponse<Void>> handleNoRoute() {
        return fail(ErrorCode.ROUTE_NOT_FOUND);
    }

    /** 兜底 → 500。訊息固定，不把內部細節洩漏給前端；細節寫進 log。 */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception e) {
        log.error("未預期的例外", e);
        return fail(ErrorCode.INTERNAL_ERROR);
    }

    private ResponseEntity<ApiResponse<Void>> fail(ErrorCode code) {
        return ResponseEntity.status(code.status()).body(ApiResponse.fail(code.message()));
    }
}
