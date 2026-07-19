package org.lvtn.mws.domain.model;

import java.time.LocalDate;

/**
 * [MỤC 6] Một điểm của báo cáo Xuất-Nhập-Tồn theo ngày.
 *  - inQty/outQty: nhập/xuất phát sinh trong ngày.
 *  - closingQty: tồn cuối ngày = tồn đầu kỳ (trước 'from') + cộng dồn (nhập - xuất) tới hết ngày này.
 */
public record StockSummaryPoint(LocalDate date, long inQty, long outQty, long closingQty) {
}
