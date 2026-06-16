package org.lvtn.mws.infrastructure.persistence.repository.role;

import org.lvtn.mws.infrastructure.persistence.entity.RolePermissionDbEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaRolePermissionRepository extends JpaRepository<RolePermissionDbEntity, RolePermissionDbEntity.RolePermissionId> {
    List<RolePermissionDbEntity> findByIdRoleId(String roleId);
    void deleteByIdRoleId(String roleId);
}
