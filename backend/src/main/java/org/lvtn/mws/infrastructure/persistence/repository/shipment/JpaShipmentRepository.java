package org.lvtn.mws.infrastructure.persistence.repository.shipment;

import org.lvtn.mws.infrastructure.persistence.entity.ShipmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaShipmentRepository extends JpaRepository<ShipmentEntity, String> {
    Optional<ShipmentEntity> findBySalesOrderId(String salesOrderId);
    Optional<ShipmentEntity> findByTransferOrderId(String transferOrderId);
    boolean existsByShipmentNumber(String shipmentNumber);
    long countByShipmentNumberStartingWith(String prefix);
}
