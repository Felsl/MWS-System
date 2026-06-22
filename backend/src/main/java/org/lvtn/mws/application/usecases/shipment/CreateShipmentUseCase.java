package org.lvtn.mws.application.usecases.shipment;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.Shipment;
import org.lvtn.mws.domain.service.ShipmentDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CreateShipmentUseCase {

    private final ShipmentDomainService shipmentDomainService;

    public Shipment execute(String salesOrderId, String carrierId) {
        return shipmentDomainService.createForSalesOrder(salesOrderId, carrierId);
    }
}
