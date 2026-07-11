package org.lvtn.mws.application.usecases.transfer;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.TransferOrder;
import org.lvtn.mws.domain.service.TransferOrderDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.lvtn.mws.infrastructure.security.scope.CreationDateScope;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetTransferOrderByIdUseCase {

    private final TransferOrderDomainService transferOrderDomainService;
    private final CreationDateScope creationDateScope;

    public TransferOrder execute(String transferId) {
        TransferOrder rec = transferOrderDomainService.findById(transferId);
        creationDateScope.assertVisible(rec.getCreatedAt());
        return rec;
    }
}
