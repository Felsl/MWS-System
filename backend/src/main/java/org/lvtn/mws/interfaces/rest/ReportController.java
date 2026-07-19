package org.lvtn.mws.interfaces.rest;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.application.usecases.report.GetStockSummaryUseCase;
import org.lvtn.mws.interfaces.dto.response.report.StockSummaryResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * [MỤC 6] Báo cáo tổng hợp. Dùng quyền INVENTORY_VIEW (báo cáo tồn là một dạng xem tồn kho);
 * đổi sang quyền REPORT_VIEW riêng nếu về sau tách quyền báo cáo.
 */
@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('INVENTORY_VIEW')")
public class ReportController {

    private final GetStockSummaryUseCase getStockSummaryUseCase;

    @GetMapping("/stock-summary")
    public List<StockSummaryResponse> stockSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String warehouseId) {
        return getStockSummaryUseCase.execute(from, to, warehouseId).stream()
                .map(p -> new StockSummaryResponse(p.date(), p.inQty(), p.outQty(), p.closingQty()))
                .toList();
    }
}
