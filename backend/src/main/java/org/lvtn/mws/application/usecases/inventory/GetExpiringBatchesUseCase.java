package org.lvtn.mws.application.usecases.inventory;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.InventoryBatch;
import org.lvtn.mws.domain.service.InventoryDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * [MỤC 6] Danh sách lô hàng còn tồn sắp hết hạn — phục vụ thẻ cảnh báo trên Dashboard.
 */
@Service
@RequiredArgsConstructor
public class GetExpiringBatchesUseCase {
    private final InventoryDomainService domainService;

    @Transactional(readOnly = true)
    public List<InventoryBatch> execute(int days, String warehouseId) {
        return domainService.findExpiringBatches(days, warehouseId);
    }
}
