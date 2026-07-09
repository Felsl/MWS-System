package org.lvtn.mws.application.usecases.transfer;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.PickingList;
import org.lvtn.mws.domain.service.TransferPickingDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Xác nhận quét lô cho một dòng gom hàng điều chuyển. */
@Service
@RequiredArgsConstructor
public class ConfirmTransferPickUseCase {

    private final TransferPickingDomainService transferPickingDomainService;

    @Transactional
    public PickingList execute(String pickingListDetailId, String scannedBatchNumberOrId, String confirmedBy) {
        return transferPickingDomainService.confirmScanForTransfer(
                pickingListDetailId, scannedBatchNumberOrId, confirmedBy);
    }
}
