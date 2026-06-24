package org.lvtn.mws.domain.repository;

import org.lvtn.mws.domain.model.Notification;

import java.util.List;
import java.util.Optional;

/** [GIAI ĐOẠN 7] Port lưu trữ thông báo. */
public interface INotificationRepository {
    Notification save(Notification notification);
    Optional<Notification> findById(String id);
    /** Thông báo của một user (gồm cả thông báo chung user_id IS NULL), mới nhất trước. */
    List<Notification> findInboxForUser(String userId);
    long countUnreadForUser(String userId);
    /** Đánh dấu đã đọc toàn bộ thông báo cá nhân của user; trả số dòng cập nhật. */
    int markAllReadForUser(String userId);
}
