package org.lvtn.mws.application.usecases.transfer;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.NewTransferLine;
import org.lvtn.mws.domain.model.TransferOrder;
import org.lvtn.mws.domain.service.TransferOrderDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CreateTransferOrderUseCase {

    private final TransferOrderDomainService transferOrderDomainService;

    public TransferOrder execute(String fromWarehouseId,
                                 String toWarehouseId,
                                 String createdBy,
                                 List<NewTransferLine> lines) {
        return transferOrderDomainService.createTransferOrder(fromWarehouseId, toWarehouseId, createdBy, lines);
    }
}
