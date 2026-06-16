//package org.lvtn.mws.infrastructure.persistence.repository.warehouse;
//
//import org.lvtn.mws.infrastructure.persistence.entity.UserWarehouseAccessDbEntity;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Modifying;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//
//import java.util.List;
//
//public interface UserWarehouseJpaRepository
//        extends JpaRepository<UserWarehouseAccessDbEntity, UserWarehouseAccessDbEntity.UserWarehouseId> {
//
//    @Query("SELECT u.id.warehouseId FROM UserWarehouseAccessEntity u WHERE u.id.userId = :userId")
//    List<String> findWarehouseIdsByUserId(@Param("userId") String userId);
//
//    @Query("SELECT a.id.warehouseId FROM UserWarehouseAccessEntity a " +
//            "JOIN UserDbEntity u ON u.id = a.id.userId " +
//            "WHERE u.username = :username AND u.deletedAt IS NULL")
//    List<String> findWarehouseIdsByUsername(@Param("username") String username);
//
//    boolean existsByIdUserIdAndIdWarehouseId(String userId, String warehouseId);
//
//    @Modifying
//    @Query("DELETE FROM UserWarehouseAccessEntity u WHERE u.id.userId = :userId")
//    void deleteAllByUserId(@Param("userId") String userId);
//
//    @Modifying
//    @Query("DELETE FROM UserWarehouseAccessEntity u " +
//            "WHERE u.id.userId = :userId AND u.id.warehouseId = :warehouseId")
//    void deleteByUserIdAndWarehouseId(@Param("userId") String userId,
//                                      @Param("warehouseId") String warehouseId);
//}

package org.lvtn.mws.infrastructure.persistence.repository.warehouse;

import org.lvtn.mws.infrastructure.persistence.entity.UserWarehouseAccessDbEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserWarehouseJpaRepository
        extends JpaRepository<UserWarehouseAccessDbEntity, UserWarehouseAccessDbEntity.UserWarehouseId> {

    @Query(value = "SELECT warehouse_id FROM user_warehouse_access WHERE user_id = :userId",
            nativeQuery = true)
    List<String> findWarehouseIdsByUserId(@Param("userId") String userId);

    @Query(value = "SELECT a.warehouse_id FROM user_warehouse_access a " +
            "JOIN users u ON u.id = a.user_id " +
            "WHERE u.username = :username AND u.deleted_at IS NULL",
            nativeQuery = true)
    List<String> findWarehouseIdsByUsername(@Param("username") String username);

    boolean existsByIdUserIdAndIdWarehouseId(String userId, String warehouseId);

    @Modifying
    @Query(value = "DELETE FROM user_warehouse_access WHERE user_id = :userId",
            nativeQuery = true)
    void deleteAllByUserId(@Param("userId") String userId);

    @Modifying
    @Query(value = "DELETE FROM user_warehouse_access WHERE user_id = :userId AND warehouse_id = :warehouseId",
            nativeQuery = true)
    void deleteByUserIdAndWarehouseId(@Param("userId") String userId,
                                      @Param("warehouseId") String warehouseId);
}