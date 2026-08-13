package org.lvtn.mws.application.usecases.inventory;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.InventoryBatch;
import org.lvtn.mws.domain.repository.IInventoryBatchRepository;
import org.lvtn.mws.domain.repository.ISupplierRepository;
import org.lvtn.mws.infrastructure.security.scope.WarehouseAccessGuard;
import org.lvtn.mws.interfaces.dto.response.inventory.AvailableBySupplierResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * [Bán theo NCC] Tồn KHẢ DỤNG (quantity − reserved) gom theo NCC cho (sản phẩm, kho).
 * Tính sống từ inventory_batches (đã gắn supplier_id ở Pha 0). Chỉ trả nhóm còn khả dụng > 0.
 * Dùng cho dropdown chọn NCC ở màn tạo đơn bán.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetAvailableBySupplierUseCase {

    private final IInventoryBatchRepository batchRepository;
    private final ISupplierRepository supplierRepository;
    private final WarehouseAccessGuard warehouseAccessGuard;

    public List<AvailableBySupplierResponse> execute(String productId, String warehouseId) {
        // A2: chặn xem dữ liệu kho ngoài phạm vi được giao (403).
        warehouseAccessGuard.check(warehouseId);

        // Gom availableQuantity() theo supplierId (giữ thứ tự xuất hiện).
        Map<String, Integer> availBySupplier = new LinkedHashMap<>();
        for (InventoryBatch b : batchRepository.findByProductIdAndWarehouseId(productId, warehouseId)) {
            if (b.getStatus() != InventoryBatch.Status.ACTIVE) continue;
            // Loại lô đã hết hạn (status có thể vẫn ACTIVE nhưng expiry_date đã qua).
            if (b.getExpiryDate() != null && b.getExpiryDate().isBefore(LocalDate.now())) continue;
            int avail = b.availableQuantity();
            if (avail <= 0) continue;
            availBySupplier.merge(b.getSupplierId(), avail, Integer::sum);
        }

        List<AvailableBySupplierResponse> result = new ArrayList<>();
        for (Map.Entry<String, Integer> e : availBySupplier.entrySet()) {
            String supplierId = e.getKey();
            String supplierName = supplierId == null ? null
                    : supplierRepository.findById(supplierId).map(s -> s.getName()).orElse(supplierId);
            result.add(new AvailableBySupplierResponse(supplierId, supplierName, e.getValue()));
        }
        return result;
    }
}
