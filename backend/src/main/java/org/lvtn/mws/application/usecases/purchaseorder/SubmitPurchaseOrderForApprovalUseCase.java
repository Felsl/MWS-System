package org.lvtn.mws.application.usecases.purchaseorder;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.application.ports.INotificationRecipientResolver;
import org.lvtn.mws.application.usecases.notification.CreateNotificationUseCase;
import org.lvtn.mws.domain.model.Notification;
import org.lvtn.mws.domain.model.PurchaseOrder;
import org.lvtn.mws.domain.service.PurchaseOrderDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Gửi PO đi duyệt (-> PENDING_APPROVAL).
 *
 * [GIAI ĐOẠN 7] Sau khi chuyển trạng thái, thông báo cho người có quyền INBOUND_APPROVE_PO
 * để vào duyệt. Thông báo tạo trong cùng transaction; nếu submit lỗi/rollback thì không gửi.
 */
@Service
@RequiredArgsConstructor
public class SubmitPurchaseOrderForApprovalUseCase {

    private static final String PERM_APPROVER = "INBOUND_APPROVE_PO";
    private static final String REFERENCE_TYPE = "PURCHASE_ORDER";

    private final PurchaseOrderDomainService domainService;
    private final INotificationRecipientResolver recipientResolver;
    private final CreateNotificationUseCase createNotificationUseCase;

    @Transactional
    public PurchaseOrder execute(String poId) {
        PurchaseOrder po = domainService.submitForApproval(poId);

        List<String> approvers = recipientResolver.resolveByPermission(PERM_APPROVER);
        createNotificationUseCase.createForUsers(
                approvers,
                "PO chờ duyệt",
                "Đơn mua " + po.getPoNumber() + " đang chờ bạn phê duyệt.",
                Notification.Type.INFO,
                REFERENCE_TYPE,
                po.getId());

        return po;
    }
}
