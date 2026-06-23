package org.lvtn.mws.domain.model;

/**
 * Ném ra khi một kho đang ở trạng thái "đóng băng" (có phiên kiểm kê FREEZED)
 * nhưng có thao tác Nhập/Xuất/Điều chuyển cố gắng thay đổi tồn kho.
 * Được {@code GlobalExceptionHandler} ánh xạ sang HTTP 409 (Conflict).
 */
public class WarehouseFrozenException extends RuntimeException {
    public WarehouseFrozenException(String message) {
        super(message);
    }
}
