package org.lvtn.mws.domain.repository;

import org.lvtn.mws.domain.model.PurchaseOrderDetail;
import java.util.List;
import java.util.Optional;

public interface IPurchaseOrderDetailRepository {
    PurchaseOrderDetail save(PurchaseOrderDetail detail);
    void saveAll(List<PurchaseOrderDetail> details);
    Optional<PurchaseOrderDetail> findById(String id);
    List<PurchaseOrderDetail> findByPoId(String poId);
    /** Xoá toàn bộ dòng của một PO — dùng khi sửa đơn (thay mới toàn bộ dòng hàng). */
    void deleteByPoId(String poId);
}
