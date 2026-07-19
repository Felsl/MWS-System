package org.lvtn.mws.domain.model;

import java.time.LocalDate;

/**
 * [MỤC 6] Một dòng gộp biến động kho theo ngày: tổng nhập và tổng xuất (số dương).
 * Là kết quả thô từ GROUP BY DATE(created_at) trên stock_movements.
 */
public record DailyStockFlow(LocalDate date, long inQty, long outQty) {
}
