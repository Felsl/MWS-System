package org.lvtn.mws.domain.service;

import org.lvtn.mws.domain.model.DailyStockFlow;
import org.lvtn.mws.domain.model.StockSummaryPoint;
import org.lvtn.mws.domain.repository.IStockMovementRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * [MỤC 6] Báo cáo Xuất-Nhập-Tồn theo ngày.
 *
 * Tồn cuối mỗi ngày (closingQty) = tồn đầu kỳ (tổng biến động TRƯỚC 'from')
 * cộng dồn (nhập - xuất) tới hết ngày đó. Nhờ vậy đường "Tồn" phản ánh mức tồn
 * thật, không phải chỉ số cộng dồn trong khoảng. Những ngày không có biến động
 * vẫn được điền (nhập=xuất=0) để đường liền mạch và tồn được mang sang.
 */
public class ReportDomainService {

    private final IStockMovementRepository stockMovementRepository;

    public ReportDomainService(IStockMovementRepository stockMovementRepository) {
        this.stockMovementRepository = stockMovementRepository;
    }

    public List<StockSummaryPoint> stockSummary(LocalDate from, LocalDate to, String warehouseId) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("Khoảng ngày (from, to) là bắt buộc");
        }
        if (to.isBefore(from)) {
            throw new IllegalArgumentException("'to' phải >= 'from'");
        }

        long opening = stockMovementRepository.sumQuantityChangeBefore(from.atStartOfDay(), warehouseId);

        List<DailyStockFlow> flows = stockMovementRepository.aggregateDailyFlow(
                from.atStartOfDay(), to.plusDays(1).atStartOfDay(), warehouseId);
        Map<LocalDate, DailyStockFlow> byDate = new HashMap<>();
        for (DailyStockFlow f : flows) byDate.put(f.date(), f);

        List<StockSummaryPoint> points = new ArrayList<>();
        long running = opening;
        for (LocalDate d = from; !d.isAfter(to); d = d.plusDays(1)) {
            DailyStockFlow f = byDate.get(d);
            long in = f == null ? 0 : f.inQty();
            long out = f == null ? 0 : f.outQty();
            running += in - out;
            points.add(new StockSummaryPoint(d, in, out, running));
        }
        return points;
    }
}
