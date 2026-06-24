package org.lvtn.mws.application.usecases.notification;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.repository.INotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** [GIAI ĐOẠN 7] Đánh dấu đã đọc toàn bộ thông báo cá nhân của người dùng hiện tại. */
@Service
@RequiredArgsConstructor
public class MarkAllNotificationsReadUseCase {

    private final INotificationRepository notificationRepository;

    @Transactional
    public int execute(String requesterUserId) {
        return notificationRepository.markAllReadForUser(requesterUserId);
    }
}
