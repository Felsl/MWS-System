package org.lvtn.mws.domain.repository;

import org.lvtn.mws.domain.model.StockMovement;
import java.util.List;

public interface IStockMovementRepository {
    StockMovement save(StockMovement movement);
    /** Ghi nhiều thẻ kho một lượt (vd thẻ song phương khi điều chuyển). */
    void appendAll(List<StockMovement> movements);
    List<StockMovement> findByReference(String referenceType, String referenceId);
    /** [GIAI ĐOẠN 6] Truy vết thẻ kho theo sản phẩm (audit trail). */
    List<StockMovement> findByProductId(String productId);
    /**
     * [A2] Truy vết theo sản phẩm nhưng CHỈ trong các kho user được phép.
     * Danh sách kho được đọc từ WarehouseScopeContext (nạp bởi @WarehouseScoped).
     * Nếu context rỗng (ADMIN) → trả toàn bộ như findByProductId.
     */
    List<StockMovement> findByProductIdScoped(String productId);
    /** [B4] Phân trang KEYSET theo sản phẩm (đã lọc phạm vi kho), created_at giảm dần. */
    org.lvtn.mws.domain.common.CursorPage<StockMovement> findByProductScrolling(
            String productId, String warehouseId, String cursor, int size);
    /** [GIAI ĐOẠN 7] Truy vết theo sản phẩm TẠI một kho cụ thể (created_at giảm dần). */
    List<StockMovement> findByProductIdAndWarehouseId(String productId, String warehouseId);
}
