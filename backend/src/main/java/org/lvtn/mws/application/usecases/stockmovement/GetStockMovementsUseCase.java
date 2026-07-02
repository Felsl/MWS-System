package org.lvtn.mws.application.usecases.stockmovement;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.StockMovement;
import org.lvtn.mws.domain.repository.IStockMovementRepository;
import org.lvtn.mws.infrastructure.security.scope.WarehouseAccessGuard;
import org.lvtn.mws.infrastructure.security.scope.WarehouseScoped;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Truy vết thẻ kho (Audit Trail): theo sản phẩm hoặc theo chứng từ gốc. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetStockMovementsUseCase {

    private final IStockMovementRepository stockMovementRepository;
    private final WarehouseAccessGuard warehouseAccessGuard;

    /**
     * A2: @WarehouseScoped nạp danh sách kho được phép vào context; repository
     * dùng WarehouseScopeSpecs để tự lọc warehouse_id IN (allowed).
     * ADMIN (context rỗng) → thấy toàn bộ.
     */
    @WarehouseScoped
    public List<StockMovement> byProduct(String productId) {
        return stockMovementRepository.findByProductIdScoped(productId);
    }

    /**
     * A2 + B4: tra thẻ kho theo sản phẩm với phân trang KEYSET (bảng lớn, không OFFSET).
     * Nếu truyền warehouseId, chặn tra kho ngoài phạm vi (403) rồi giới hạn trong kho đó.
     */
    @WarehouseScoped
    public org.lvtn.mws.domain.common.CursorPage<StockMovement> byProductScrolling(
            String productId, String warehouseId, String cursor, int size) {
        if (warehouseId != null && !warehouseId.isBlank()) {
            warehouseAccessGuard.check(warehouseId);
        }
        return stockMovementRepository.findByProductScrolling(productId, warehouseId, cursor, size);
    }

    public List<StockMovement> byProductAndWarehouse(String productId, String warehouseId) {
        // A2: chặn tra thẻ kho của kho ngoài phạm vi được giao (403).
        warehouseAccessGuard.check(warehouseId);
        return stockMovementRepository.findByProductIdAndWarehouseId(productId, warehouseId);
    }

    public List<StockMovement> byReference(String referenceType, String referenceId) {
        return stockMovementRepository.findByReference(referenceType, referenceId);
    }
}
