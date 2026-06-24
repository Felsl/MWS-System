package org.lvtn.mws.infrastructure.persistence.repository.transfer;

import org.lvtn.mws.infrastructure.persistence.entity.CarrierEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaCarrierRepository extends JpaRepository<CarrierEntity, String> {
    boolean existsByCode(String code);
}
