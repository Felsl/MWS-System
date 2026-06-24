package org.lvtn.mws.application.usecases.notification;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.Notification;
import org.lvtn.mws.domain.repository.INotificationRepository;
import org.lvtn.mws.domain.service.NotificationDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** [GIAI ĐOẠN 7] Đánh dấu một thông báo là đã đọc (kiểm quyền sở hữu). */
@Service
@RequiredArgsConstructor
public class MarkNotificationReadUseCase {

    private final INotificationRepository notificationRepository;
    private final NotificationDomainService notificationDomainService;

    @Transactional
    public Notification execute(String notificationId, String requesterUserId) {
        Notification n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông báo: " + notificationId));
        notificationDomainService.assertCanRead(n, requesterUserId);
        n.markRead();
        return notificationRepository.save(n);
    }
}
