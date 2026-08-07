package org.lvtn.mws.domain.model;

/** [PA1] Đưa hàng vào ô kệ vượt sức chứa cấu hình (kg / thể tích). Map -> HTTP 409. */
public class BinCapacityExceededException extends RuntimeException {
    public BinCapacityExceededException(String message) { super(message); }
}
