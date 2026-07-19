package org.lvtn.mws.application.usecases.report;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.StockSummaryPoint;
import org.lvtn.mws.domain.service.ReportDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * [MỤC 6] Báo cáo Xuất-Nhập-Tồn theo ngày cho Dashboard.
 */
@Service
@RequiredArgsConstructor
public class GetStockSummaryUseCase {
    private final ReportDomainService reportDomainService;

    @Transactional(readOnly = true)
    public List<StockSummaryPoint> execute(LocalDate from, LocalDate to, String warehouseId) {
        return reportDomainService.stockSummary(from, to, warehouseId);
    }
}
