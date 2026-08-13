package org.lvtn.mws.application.usecases.purchaseorder;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.PurchaseOrder;
import org.lvtn.mws.domain.repository.INotificationRepository;
import org.lvtn.mws.domain.service.PurchaseOrderDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ApprovePurchaseOrderUseCase {
    private final PurchaseOrderDomainService domainService;
    private final INotificationRepository notificationRepository;

    @Transactional
    public PurchaseOrder execute(String poId, String approvedBy) {
        PurchaseOrder po = domainService.approve(poId, approvedBy);
        // Ẩn thông báo "PO chờ duyệt" của PO này (đã duyệt xong).
        notificationRepository.deleteByReference("PURCHASE_ORDER", poId);
        return po;
    }
}
