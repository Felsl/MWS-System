package org.lvtn.mws.infrastructure.persistence.repository.notification;

import org.lvtn.mws.infrastructure.persistence.entity.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * [GIAI ĐOẠN 7] Native SQL theo chính sách dự án (tránh phụ thuộc tên class entity trong JPQL phức tạp).
 */
public interface JpaNotificationRepository extends JpaRepository<NotificationEntity, String> {

    @Query(value = "SELECT * FROM notifications " +
            "WHERE (user_id = :userId OR user_id IS NULL) " +
            "ORDER BY created_at DESC", nativeQuery = true)
    List<NotificationEntity> findInboxForUser(@Param("userId") String userId);

    @Query(value = "SELECT COUNT(*) FROM notifications " +
            "WHERE (user_id = :userId OR user_id IS NULL) AND is_read = FALSE", nativeQuery = true)
    long countUnreadForUser(@Param("userId") String userId);

    @Modifying
    @Query(value = "UPDATE notifications SET is_read = TRUE " +
            "WHERE user_id = :userId AND is_read = FALSE", nativeQuery = true)
    int markAllReadForUser(@Param("userId") String userId);
}
