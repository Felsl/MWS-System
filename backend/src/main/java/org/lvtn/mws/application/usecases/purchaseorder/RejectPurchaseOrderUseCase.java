package org.lvtn.mws.application.usecases.purchaseorder;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.PurchaseOrder;
import org.lvtn.mws.domain.repository.INotificationRepository;
import org.lvtn.mws.domain.service.PurchaseOrderDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RejectPurchaseOrderUseCase {
    private final PurchaseOrderDomainService domainService;
    private final INotificationRepository notificationRepository;

    @Transactional
    public PurchaseOrder execute(String poId) {
        PurchaseOrder po = domainService.reject(poId);
        // Ẩn thông báo "PO chờ duyệt" của PO này (đã từ chối, hết chờ duyệt).
        notificationRepository.deleteByReference("PURCHASE_ORDER", poId);
        return po;
    }
}
