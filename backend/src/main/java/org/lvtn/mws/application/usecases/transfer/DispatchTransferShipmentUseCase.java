package org.lvtn.mws.application.usecases.transfer;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.Shipment;
import org.lvtn.mws.domain.service.TransferOrderDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class DispatchTransferShipmentUseCase {

    private final TransferOrderDomainService transferOrderDomainService;

    public Shipment execute(String transferId, String carrierId) {
        return transferOrderDomainService.dispatchTransferShipment(transferId, carrierId);
    }
}
