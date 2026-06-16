package org.lvtn.mws.infrastructure.persistence.repository.permission;

import org.lvtn.mws.infrastructure.persistence.entity.PermissionDbEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface PermissionJpaRepository extends JpaRepository<PermissionDbEntity, String> {
    Optional<PermissionDbEntity> findByCode(String code);
    List<PermissionDbEntity> findByModule(String module);
    boolean existsByCode(String code);
    boolean existsByCodeAndIdNot(String code, String id);
    List<PermissionDbEntity> findByIdIn(List<String> ids);
    @Query("SELECT p FROM PermissionDbEntity p WHERE p.id IN :ids")
    List<PermissionDbEntity> findByIds(@Param("ids") Set<String> ids);
}
