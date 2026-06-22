package org.lvtn.mws.application.usecases.shipment;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.Shipment;
import org.lvtn.mws.domain.service.ShipmentDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetShipmentByIdUseCase {

    private final ShipmentDomainService shipmentDomainService;

    public Shipment execute(String shipmentId) {
        return shipmentDomainService.getById(shipmentId);
    }
}
