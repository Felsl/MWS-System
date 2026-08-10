package org.lvtn.mws.application.usecases.purchaseorder;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.PurchaseOrder;
import org.lvtn.mws.domain.model.PurchaseOrderLineCommand;
import org.lvtn.mws.domain.service.PurchaseOrderDomainService;
import org.lvtn.mws.infrastructure.security.scope.WarehouseAccessGuard;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Sửa một đơn mua chưa được duyệt (kho/ngày dự kiến/dòng hàng).
 * Quyền: chỉ ADMIN (ROLE_ADMIN) — enforce ở controller bằng @PreAuthorize.
 */
@Service
@RequiredArgsConstructor
public class UpdatePurchaseOrderUseCase {
    private final PurchaseOrderDomainService domainService;
    private final WarehouseAccessGuard warehouseAccessGuard;

    @Transactional
    public PurchaseOrder execute(String poId, String supplierId, String warehouseId,
                                 LocalDate expectedDate, List<PurchaseOrderLineCommand> lines) {
        // Chặn sửa để đẩy hàng về kho ngoài phạm vi được giao (403), giống lúc tạo.
        warehouseAccessGuard.check(warehouseId);
        return domainService.update(poId, supplierId, warehouseId, expectedDate, lines);
    }
}
