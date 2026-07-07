package org.lvtn.mws.domain.repository;

import org.lvtn.mws.domain.model.PurchaseOrder;
import java.util.List;
import java.util.Optional;

public interface IPurchaseOrderRepository {
    PurchaseOrder save(PurchaseOrder purchaseOrder);
    Optional<PurchaseOrder> findById(String id);
    Optional<PurchaseOrder> findByPoNumber(String poNumber);
    List<PurchaseOrder> findAll();
    /** [B4] Tìm kiếm (mã PO + trạng thái) + phân trang, đã lọc theo phạm vi kho. */
    org.lvtn.mws.domain.common.PageResult<PurchaseOrder> search(
            String keyword, String status, org.lvtn.mws.domain.common.PageQuery pageQuery);
    boolean existsByPoNumber(String poNumber);
}
