package org.lvtn.mws.application.usecases.purchaseorder;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.common.PageQuery;
import org.lvtn.mws.domain.common.PageResult;
import org.lvtn.mws.domain.model.PurchaseOrder;
import org.lvtn.mws.domain.service.PurchaseOrderDomainService;
import org.lvtn.mws.infrastructure.security.scope.WarehouseScoped;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetAllPurchaseOrdersUseCase {

    private final PurchaseOrderDomainService purchaseOrderDomainService;

    /** B4: tìm kiếm + phân trang đơn mua, đã lọc theo kho user được phép (A2). */
    @WarehouseScoped
    public PageResult<PurchaseOrder> execute(String keyword, String status, int page, int size) {
        return purchaseOrderDomainService.search(keyword, status, new PageQuery(page, size));
    }
}
