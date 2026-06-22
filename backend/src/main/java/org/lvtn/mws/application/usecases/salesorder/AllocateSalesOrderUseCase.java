package org.lvtn.mws.application.usecases.salesorder;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.SalesOrder;
import org.lvtn.mws.domain.service.SalesOrderDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Gửi duyệt / Xác nhận đơn -> ALLOCATED (phân bổ hàng ảo).
 * @Transactional bao trọn: nếu reserveStock ném InsufficientStockException -> rollback toàn bộ.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AllocateSalesOrderUseCase {

    private final SalesOrderDomainService salesOrderDomainService;

    public SalesOrder execute(String soId) {
        return salesOrderDomainService.allocate(soId);
    }
}
