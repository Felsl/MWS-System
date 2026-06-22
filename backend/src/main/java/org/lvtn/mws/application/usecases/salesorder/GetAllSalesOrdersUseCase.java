package org.lvtn.mws.application.usecases.salesorder;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.SalesOrder;
import org.lvtn.mws.domain.service.SalesOrderDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetAllSalesOrdersUseCase {

    private final SalesOrderDomainService salesOrderDomainService;

    public List<SalesOrder> execute() {
        return salesOrderDomainService.findAll();
    }
}
