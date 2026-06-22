package org.lvtn.mws.application.usecases.shipment;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.Shipment;
import org.lvtn.mws.domain.service.ShipmentDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetAllShipmentsUseCase {
    private final ShipmentDomainService shipmentDomainService;

    public List<Shipment> execute() {
        return shipmentDomainService.findAll();
    }
}
