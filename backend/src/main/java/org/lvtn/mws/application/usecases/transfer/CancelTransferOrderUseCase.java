package org.lvtn.mws.application.usecases.transfer;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.TransferOrder;
import org.lvtn.mws.domain.service.TransferOrderDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Huỷ phiếu điều chuyển. Nhả lại reserved_quantity kho nguồn nếu đã giữ chỗ ảo.
 * Chặn huỷ khi phiếu đã IN_TRANSIT/COMPLETED (do domain model đảm bảo).
 */
@Service
@RequiredArgsConstructor
@Transactional
public class CancelTransferOrderUseCase {

    private final TransferOrderDomainService transferOrderDomainService;

    public TransferOrder execute(String transferId) {
        return transferOrderDomainService.cancelTransfer(transferId);
    }
}
