package org.lvtn.mws.application.usecases.transfer;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.TransferOrder;
import org.lvtn.mws.domain.service.TransferOrderDomainService;
import org.lvtn.mws.domain.service.TransferPickingDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ApproveTransferOrderUseCase {

    private final TransferOrderDomainService transferOrderDomainService;
    private final TransferPickingDomainService transferPickingDomainService;

    public TransferOrder execute(String transferId, String approvedBy) {
        TransferOrder order = transferOrderDomainService.approveTransferOrder(transferId, approvedBy);
        // Duyệt xong TỰ phân bổ tồn (FEFO kho nguồn) + tạo lệnh lấy hàng DÙNG CHUNG
        // (APPROVED -> PICKING). Lệnh này xuất hiện trong "Lệnh lấy hàng" chung như SO,
        // dùng chung endpoint picking-lists (gán/quét/báo thiếu/hoàn thành).
        transferPickingDomainService.generateForTransfer(order.getId());
        return transferOrderDomainService.findById(order.getId());
    }
}
