package org.lvtn.mws.application.usecases.transfer;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.TransferOrder;
import org.lvtn.mws.domain.service.TransferOrderDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class RequestTransferApprovalUseCase {

    private final TransferOrderDomainService transferOrderDomainService;

    public TransferOrder execute(String transferId) {
        return transferOrderDomainService.requestTransferApproval(transferId);
    }
}
