package org.lvtn.mws.application.event;

import org.lvtn.mws.domain.model.StockMovement;

/**
 * [GIAI ĐOẠN 7] Sự kiện biến động tồn kho theo mô hình Publisher–Subscriber.
 *
 * Được phát ra ở chặng cuối của một luồng nghiệp vụ ĐÃ ghi tồn thành công (trong transaction).
 * Mang theo bản ghi {@link StockMovement} đã dựng sẵn (chưa persist).
 *
 * Người nghe (AFTER_COMMIT):
 *   - StockMovementAuditListener : ghi thẻ kho (audit trail) sau khi đơn gốc commit 100%.
 *   - SafetyStockAlertListener   : với biến động GIẢM, kiểm tra ngưỡng tồn an toàn để cảnh báo.
 *
 * Dùng AFTER_COMMIT nên nếu transaction gốc rollback thì KHÔNG ghi thẻ kho / KHÔNG cảnh báo,
 * tránh "lịch sử ma".
 */
public record StockMovementEvent(StockMovement movement) {
}
