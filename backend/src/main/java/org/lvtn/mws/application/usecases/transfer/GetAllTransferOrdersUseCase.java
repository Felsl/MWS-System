package org.lvtn.mws.application.usecases.transfer;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.TransferOrder;
import org.lvtn.mws.domain.service.TransferOrderDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetAllTransferOrdersUseCase {

    private final TransferOrderDomainService transferOrderDomainService;

    public List<TransferOrder> execute() {
        return transferOrderDomainService.findAll();
    }
}
