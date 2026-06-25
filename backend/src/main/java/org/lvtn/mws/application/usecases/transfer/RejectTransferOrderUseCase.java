package org.lvtn.mws.application.usecases.transfer;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.TransferOrder;
import org.lvtn.mws.domain.service.TransferOrderDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Từ chối duyệt phiếu điều chuyển (chỉ từ PENDING_APPROVAL).
 * Nhả lại reserved_quantity kho nguồn vì lúc gửi duyệt đã giữ chỗ ảo.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class RejectTransferOrderUseCase {

    private final TransferOrderDomainService transferOrderDomainService;

    public TransferOrder execute(String transferId, String rejectedBy) {
        return transferOrderDomainService.rejectTransfer(transferId, rejectedBy);
    }
}
