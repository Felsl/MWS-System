package org.lvtn.mws.application.usecases.transfer;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.TransferOrder;
import org.lvtn.mws.domain.model.TransferReceiptLine;
import org.lvtn.mws.domain.service.TransferOrderDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CompleteTransferReceiptUseCase {

    private final TransferOrderDomainService transferOrderDomainService;

    public TransferOrder execute(String transferId, List<TransferReceiptLine> receiptLines) {
        return transferOrderDomainService.completeTransferReceipt(transferId, receiptLines);
    }
}
