package org.lvtn.mws.infrastructure.persistence.repository.warehouse;

import org.lvtn.mws.infrastructure.persistence.entity.WarehouseDbEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JpaWarehouseRepository extends JpaRepository<WarehouseDbEntity, String> {
    Optional<WarehouseDbEntity> findByIdAndDeletedAtIsNull(String id);
    Optional<WarehouseDbEntity> findByCodeAndDeletedAtIsNull(String code);
    List<WarehouseDbEntity> findByDeletedAtIsNull();
    List<WarehouseDbEntity> findByIdInAndDeletedAtIsNull(List<String> ids);
    boolean existsByIdAndDeletedAtIsNull(String id);
    boolean existsByCodeAndDeletedAtIsNull(String code);
    boolean existsByCodeAndIdNotAndDeletedAtIsNull(String code, String id);
}
