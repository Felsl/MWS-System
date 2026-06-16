package org.lvtn.mws.infrastructure.persistence.repository.user;

import org.lvtn.mws.infrastructure.persistence.entity.UserDbEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<UserDbEntity, String> {

    Optional<UserDbEntity> findByUsernameAndDeletedAtIsNull(String username);
    Optional<UserDbEntity> findByEmailAndDeletedAtIsNull(String email);
    Optional<UserDbEntity> findByIdAndDeletedAtIsNull(String id);
    List<UserDbEntity> findAllByDeletedAtIsNull();
    List<UserDbEntity> findByRoleIdAndDeletedAtIsNull(String roleId);
    boolean existsByUsernameAndDeletedAtIsNull(String username);
    boolean existsByEmailAndDeletedAtIsNull(String email);
    boolean existsByEmailAndIdNotAndDeletedAtIsNull(String email, String id);

    // Native query để tránh lỗi UnknownEntityException như JpaRoleRepository
    @Modifying
    @Query(value = "UPDATE users SET deleted_at = :now WHERE id = :id", nativeQuery = true)
    void softDelete(@Param("id") String id, @Param("now") LocalDateTime now);

    // Dùng cho AOP Data Scope — tìm warehouse theo username
    @Query(value = "SELECT a.warehouse_id FROM user_warehouse_access a " +
            "JOIN users u ON u.id = a.user_id " +
            "WHERE u.username = :username AND u.deleted_at IS NULL",
            nativeQuery = true)
    List<String> findWarehouseIdsByUsername(@Param("username") String username);
}