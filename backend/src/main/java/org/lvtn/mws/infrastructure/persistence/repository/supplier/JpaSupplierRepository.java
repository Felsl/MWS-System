package org.lvtn.mws.infrastructure.persistence.repository.supplier;

import org.lvtn.mws.infrastructure.persistence.entity.SupplierEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaSupplierRepository extends JpaRepository<SupplierEntity, String> {
    boolean existsByCode(String code);
    List<SupplierEntity> findByDeletedAtIsNull();
}
