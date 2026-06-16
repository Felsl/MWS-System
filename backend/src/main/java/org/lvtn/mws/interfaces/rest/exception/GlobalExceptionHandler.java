package org.lvtn.mws.interfaces.rest.exception;

import lombok.extern.slf4j.Slf4j;
import org.lvtn.mws.domain.model.InsufficientStockException;
import org.lvtn.mws.interfaces.dto.response.ErrorResponse;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<ErrorResponse> handleBusinessExceptions(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ErrorResponse.builder()
                        .timestamp(LocalDateTime.now())
                        .status(HttpStatus.BAD_REQUEST.value())
                        .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                        .message(ex.getMessage())
                        .build());
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientStock(InsufficientStockException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ErrorResponse.builder()
                        .timestamp(LocalDateTime.now())
                        .status(HttpStatus.CONFLICT.value())
                        .error(HttpStatus.CONFLICT.getReasonPhrase())
                        .message(ex.getMessage())
                        .build());
    }

    /**
     * Optimistic locking: after 3 retries still fails → return 409 Conflict.
     */
    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLocking(OptimisticLockingFailureException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ErrorResponse.builder()
                        .timestamp(LocalDateTime.now())
                        .status(HttpStatus.CONFLICT.value())
                        .error(HttpStatus.CONFLICT.getReasonPhrase())
                        .message("Dữ liệu tồn kho vừa được cập nhật bởi một giao dịch khác. Vui lòng thử lại.")
                        .build());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex) {
        if (ex.getMessage() != null && ex.getMessage().contains("not found")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ErrorResponse.builder()
                            .timestamp(LocalDateTime.now())
                            .status(HttpStatus.NOT_FOUND.value())
                            .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                            .message(ex.getMessage())
                            .build());
        }
        log.error("Unhandled RuntimeException: ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ErrorResponse.builder()
                        .timestamp(LocalDateTime.now())
                        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                        .message("Internal server error")
                        .build());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ErrorResponse.builder()
                        .timestamp(LocalDateTime.now())
                        .status(HttpStatus.BAD_REQUEST.value())
                        .error("Validation Error")
                        .message("Invalid input: " + errors)
                        .build());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                ErrorResponse.builder()
                        .timestamp(LocalDateTime.now())
                        .status(HttpStatus.FORBIDDEN.value())
                        .error(HttpStatus.FORBIDDEN.getReasonPhrase())
                        .message("Access denied")
                        .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex) {
        log.error("Unexpected error: ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ErrorResponse.builder()
                        .timestamp(LocalDateTime.now())
                        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                        .message("An unexpected error occurred")
                        .build());
    }
}