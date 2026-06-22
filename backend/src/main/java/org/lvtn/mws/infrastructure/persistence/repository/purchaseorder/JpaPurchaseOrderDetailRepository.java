package org.lvtn.mws.infrastructure.persistence.repository.purchaseorder;

import org.lvtn.mws.infrastructure.persistence.entity.PurchaseOrderDetailEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaPurchaseOrderDetailRepository extends JpaRepository<PurchaseOrderDetailEntity, String> {
    List<PurchaseOrderDetailEntity> findByPoId(String poId);
}
