package org.lvtn.mws.infrastructure.persistence.repository.purchaseorder;

import org.lvtn.mws.infrastructure.persistence.entity.PurchaseOrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaPurchaseOrderRepository extends JpaRepository<PurchaseOrderEntity, String> {
    Optional<PurchaseOrderEntity> findByPoNumber(String poNumber);
    boolean existsByPoNumber(String poNumber);
}
