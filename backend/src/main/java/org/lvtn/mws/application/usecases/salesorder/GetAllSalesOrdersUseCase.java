package org.lvtn.mws.application.usecases.salesorder;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.common.PageQuery;
import org.lvtn.mws.domain.common.PageResult;
import org.lvtn.mws.domain.model.SalesOrder;
import org.lvtn.mws.domain.service.SalesOrderDomainService;
import org.lvtn.mws.infrastructure.security.scope.WarehouseScoped;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetAllSalesOrdersUseCase {

    private final SalesOrderDomainService salesOrderDomainService;

    /** B4: tìm kiếm + phân trang đơn xuất, đã lọc theo kho user được phép (A2). */
    @WarehouseScoped
    public PageResult<SalesOrder> execute(String keyword, String status, int page, int size, String sortBy, String sortDir) {
        return salesOrderDomainService.search(keyword, status, new PageQuery(page, size, sortBy, sortDir));
    }
}
