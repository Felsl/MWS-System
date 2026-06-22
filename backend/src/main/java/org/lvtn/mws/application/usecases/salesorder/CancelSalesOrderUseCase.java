package org.lvtn.mws.application.usecases.salesorder;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.SalesOrder;
import org.lvtn.mws.domain.service.SalesOrderDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CancelSalesOrderUseCase {

    private final SalesOrderDomainService salesOrderDomainService;

    public SalesOrder execute(String soId) {
        return salesOrderDomainService.cancel(soId);
    }
}
