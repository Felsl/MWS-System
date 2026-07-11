package org.lvtn.mws.application.usecases.salesorder;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.SalesOrder;
import org.lvtn.mws.domain.service.SalesOrderDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.lvtn.mws.infrastructure.security.scope.CreationDateScope;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetSalesOrderByIdUseCase {

    private final SalesOrderDomainService salesOrderDomainService;
    private final CreationDateScope creationDateScope;

    public SalesOrder execute(String id) {
        SalesOrder rec = salesOrderDomainService.findById(id);
        creationDateScope.assertVisible(rec.getCreatedAt());
        return rec;
    }
}
