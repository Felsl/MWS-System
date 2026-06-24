package org.lvtn.mws.infrastructure.persistence.repository.transfer;

import org.lvtn.mws.infrastructure.persistence.entity.ShipmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaShipmentRepository extends JpaRepository<ShipmentEntity, String> {
    Optional<ShipmentEntity> findByTransferOrderId(String transferOrderId);
}
