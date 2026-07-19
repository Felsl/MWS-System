package org.lvtn.mws.interfaces.dto.response.report;

import java.time.LocalDate;

/** [MỤC 6] Một điểm báo cáo Xuất-Nhập-Tồn: { date, inQty, outQty, closingQty }. */
public record StockSummaryResponse(
        LocalDate date,
        long inQty,
        long outQty,
        long closingQty) {
}
