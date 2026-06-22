package org.lvtn.mws.application.usecases.salesorder;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.SalesOrder;
import org.lvtn.mws.domain.model.SalesOrderLineCommand;
import org.lvtn.mws.domain.service.SalesOrderDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CreateSalesOrderUseCase {

    private final SalesOrderDomainService salesOrderDomainService;

    public SalesOrder execute(String warehouseId,
                              String customerId,
                              BigDecimal discountAmount,
                              int priority,
                              LocalDate requiredDate,
                              String createdBy,
                              List<SalesOrderLineCommand> lines) {
        return salesOrderDomainService.create(
                warehouseId, customerId, discountAmount, priority, requiredDate, createdBy, lines);
    }
}
