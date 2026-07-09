package org.lvtn.mws.application.usecases.transfer;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.PickingList;
import org.lvtn.mws.domain.service.TransferPickingDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Sinh lệnh gom hàng cho phiếu điều chuyển (APPROVED -> PICKING). */
@Service
@RequiredArgsConstructor
public class GenerateTransferPickingUseCase {

    private final TransferPickingDomainService transferPickingDomainService;

    @Transactional
    public PickingList execute(String transferOrderId) {
        return transferPickingDomainService.generateForTransfer(transferOrderId);
    }
}
