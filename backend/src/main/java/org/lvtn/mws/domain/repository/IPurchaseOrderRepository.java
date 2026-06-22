package org.lvtn.mws.domain.repository;

import org.lvtn.mws.domain.model.PurchaseOrder;
import java.util.List;
import java.util.Optional;

public interface IPurchaseOrderRepository {
    PurchaseOrder save(PurchaseOrder purchaseOrder);
    Optional<PurchaseOrder> findById(String id);
    Optional<PurchaseOrder> findByPoNumber(String poNumber);
    List<PurchaseOrder> findAll();
    boolean existsByPoNumber(String poNumber);
}
