package org.lvtn.mws.application.usecases.report;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.StockSummaryPoint;
import org.lvtn.mws.domain.service.ReportDomainService;
import org.lvtn.mws.infrastructure.security.scope.WarehouseAccessGuard;
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
    private final WarehouseAccessGuard warehouseAccessGuard;

    @Transactional(readOnly = true)
    public List<StockSummaryPoint> execute(LocalDate from, LocalDate to, String warehouseId) {
        // A2: nếu chỉ định 1 kho, chặn kho ngoài phạm vi (403).
        // Lưu ý: warehouseId=null => báo cáo toàn bộ kho (guard no-op) — xem HUONG_DAN mục caveat.
        warehouseAccessGuard.check(warehouseId);
        return reportDomainService.stockSummary(from, to, warehouseId);
    }
}
