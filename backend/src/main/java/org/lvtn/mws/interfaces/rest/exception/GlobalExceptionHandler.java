package org.lvtn.mws.interfaces.rest.exception;

import org.lvtn.mws.domain.model.InsufficientStockException;
import org.lvtn.mws.domain.model.UnauthorizedAdjustmentException;
import org.lvtn.mws.domain.model.WarehouseFrozenException;
import org.lvtn.mws.interfaces.dto.response.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * Quy đổi exception sang HTTP nhất quán.
 *   - IllegalArgumentException / IllegalStateException     -> 400 (vi phạm quy tắc nghiệp vụ)
 *   - MethodArgumentNotValidException                      -> 400 (validate request)
 *   - InsufficientStockException                           -> 400 (không đủ tồn)        [GĐ6: thêm]
 *   - DataIntegrityViolationException                      -> 400 (vi phạm ràng buộc DB) [GĐ6: thêm]
 *   - AccessDeniedException / UnauthorizedAdjustmentException -> 403                     [GĐ6: thêm]
 *   - WarehouseFrozenException                             -> 409 (kho đang kiểm kê)     [GĐ6: thêm]
 *   - OptimisticLockingFailureException                    -> 409 (tranh chấp ghi)       [GĐ6: thêm]
 *   - Exception                                            -> 500
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<ErrorResponse> handleBusinessRule(RuntimeException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(this::formatFieldError)
                .collect(Collectors.joining("; "));
        return build(HttpStatus.BAD_REQUEST, message);
    }

    // [GIAI ĐOẠN 6] Không đủ tồn (trước đây rơi vào handler 500).
    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientStock(InsufficientStockException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // [GIAI ĐOẠN 6] Vi phạm ràng buộc DB, vd chk_batch_qty (tồn lô không được âm).
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex) {
        return build(HttpStatus.BAD_REQUEST,
                "Thao tác vi phạm ràng buộc dữ liệu (vd tồn kho/tồn lô không được âm)");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        return build(HttpStatus.FORBIDDEN, "Bạn không có quyền thực hiện thao tác này");
    }

    // [GIAI ĐOẠN 6] Không đủ thẩm quyền duyệt phiếu điều chỉnh theo ngưỡng %.
    @ExceptionHandler(UnauthorizedAdjustmentException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedAdjustment(UnauthorizedAdjustmentException ex) {
        return build(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    // [GIAI ĐOẠN 6] Kho đang đóng băng vì kiểm kê.
    @ExceptionHandler(WarehouseFrozenException.class)
    public ResponseEntity<ErrorResponse> handleWarehouseFrozen(WarehouseFrozenException ex) {
        return build(HttpStatus.CONFLICT, ex.getMessage());
    }

    // [GIAI ĐOẠN 6] Tranh chấp ghi đồng thời sau khi đã hết số lần retry.
    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLock(OptimisticLockingFailureException ex) {
        return build(HttpStatus.CONFLICT,
                "Dữ liệu tồn kho vừa bị thay đổi bởi giao dịch khác, vui lòng thử lại");
    }

    /** Path không tồn tại -> 404 rõ ràng, thay vì bị catch-all biến thành 500. */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResource(NoResourceFoundException ex) {
        return build(HttpStatus.NOT_FOUND, "Không tìm thấy endpoint: " + ex.getResourcePath());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
        log.error("Lỗi không lường trước (500): {}", ex.toString(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Đã xảy ra lỗi hệ thống");
    }

    private String formatFieldError(FieldError error) {
        return error.getField() + ": " + error.getDefaultMessage();
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message) {
        ErrorResponse body = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .build();
        return ResponseEntity.status(status).body(body);
    }
}
