package org.lvtn.mws.application.usecases.inventory;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.repository.IGoodsReceiptDetailRepository;
import org.lvtn.mws.infrastructure.security.scope.WarehouseAccessGuard;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Map số lô -> tên nhà cung cấp cho (sản phẩm, kho), suy ra lúc query từ chuỗi
 * goods_receipt_details -> goods_receipts -> purchase_orders -> suppliers (KHÔNG lưu supplier trên lô).
 * Lô không truy được nguồn (tạo tay / điều chuyển / điều chỉnh) sẽ không có trong map -> FE hiện "—".
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetBatchSuppliersUseCase {

    private final IGoodsReceiptDetailRepository goodsReceiptDetailRepository;
    private final WarehouseAccessGuard warehouseAccessGuard;

    public Map<String, String> executeByProductAndWarehouse(String productId, String warehouseId) {
        // A2: chặn xem dữ liệu của kho ngoài phạm vi được giao (403).
        warehouseAccessGuard.check(warehouseId);
        return goodsReceiptDetailRepository.findBatchSuppliersByProductAndWarehouse(productId, warehouseId);
    }

    /** [PA1] Map id sản phẩm -> NCC (gộp) cho toàn kho — dùng ở màn tồn theo kho. */
    public Map<String, String> productSuppliersByWarehouse(String warehouseId) {
        warehouseAccessGuard.check(warehouseId);
        return goodsReceiptDetailRepository.findProductSuppliersByWarehouse(warehouseId);
    }
}
