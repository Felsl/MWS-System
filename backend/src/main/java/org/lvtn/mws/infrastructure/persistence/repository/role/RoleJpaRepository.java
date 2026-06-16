package org.lvtn.mws.infrastructure.persistence.repository.role;

import org.lvtn.mws.infrastructure.persistence.entity.RoleDbEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RoleJpaRepository extends JpaRepository<RoleDbEntity, String> {

    Optional<RoleDbEntity> findByCode(String code);
    boolean existsByCode(String code);
    boolean existsByCodeAndIdNot(String code, String id);

    @Modifying
    @Query(value = "INSERT INTO role_permissions (role_id, permission_id) VALUES (:roleId, :permissionId)", nativeQuery = true)
    void addPermission(@Param("roleId") String roleId, @Param("permissionId") String permissionId);

    @Modifying
    @Query(value = "DELETE FROM role_permissions WHERE role_id = :roleId AND permission_id = :permissionId", nativeQuery = true)
    void removePermission(@Param("roleId") String roleId, @Param("permissionId") String permissionId);

    @Modifying
    @Query(value = "DELETE FROM role_permissions WHERE role_id = :roleId", nativeQuery = true)
    void removeAllPermissions(@Param("roleId") String roleId);

    @Query(value = "SELECT COUNT(*) > 0 FROM users WHERE role_id = :roleId AND deleted_at IS NULL", nativeQuery = true)
    boolean isRoleInUse(@Param("roleId") String roleId);
}