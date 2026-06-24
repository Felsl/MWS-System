package org.lvtn.mws.application.usecases.notification;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.Notification;
import org.lvtn.mws.domain.repository.INotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** [GIAI ĐOẠN 7] Lấy hộp thông báo của người dùng hiện tại + số chưa đọc. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetMyNotificationsUseCase {

    private final INotificationRepository notificationRepository;

    public List<Notification> inbox(String userId) {
        return notificationRepository.findInboxForUser(userId);
    }

    public long unreadCount(String userId) {
        return notificationRepository.countUnreadForUser(userId);
    }
}
