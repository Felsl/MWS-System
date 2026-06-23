package org.lvtn.mws.domain.model;

/**
 * Ném ra khi người dùng không đủ thẩm quyền duyệt một phiếu điều chỉnh có mức
 * chênh lệch vượt ngưỡng cho phép của vai trò họ.
 * Được {@code GlobalExceptionHandler} ánh xạ sang HTTP 403 (Forbidden).
 */
public class UnauthorizedAdjustmentException extends RuntimeException {
    public UnauthorizedAdjustmentException(String message) {
        super(message);
    }
}
