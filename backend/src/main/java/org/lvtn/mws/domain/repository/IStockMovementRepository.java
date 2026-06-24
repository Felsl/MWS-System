package org.lvtn.mws.domain.repository;

import org.lvtn.mws.domain.model.StockMovement;
import java.util.List;

public interface IStockMovementRepository {
    StockMovement save(StockMovement movement);
    List<StockMovement> findByReference(String referenceType, String referenceId);
    /** [GIAI ĐOẠN 6] Truy vết thẻ kho theo sản phẩm (audit trail). */
    List<StockMovement> findByProductId(String productId);
    /** [GIAI ĐOẠN 7] Truy vết theo sản phẩm TẠI một kho cụ thể (created_at giảm dần). */
    List<StockMovement> findByProductIdAndWarehouseId(String productId, String warehouseId);
}
