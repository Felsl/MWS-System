package org.lvtn.mws.application.usecases.notification;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.application.ports.INotificationPusher;
import org.lvtn.mws.domain.model.Notification;
import org.lvtn.mws.domain.repository.INotificationRepository;
import org.lvtn.mws.domain.service.NotificationDomainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * [GIAI ĐOẠN 7] Hàm lõi tạo thông báo: dựng (domain) → lưu (repo) → đẩy real-time (pusher).
 * Dùng bởi cron quét hạn dùng, listener tồn an toàn, và các luồng workflow (gửi duyệt PO...).
 */
@Service
@RequiredArgsConstructor
public class CreateNotificationUseCase {

    private static final Logger log = LoggerFactory.getLogger(CreateNotificationUseCase.class);

    private final NotificationDomainService notificationDomainService;
    private final INotificationRepository notificationRepository;
    private final INotificationPusher notificationPusher;

    /** Tạo cho 1 người (hoặc thông báo chung khi userId = null). */
    @Transactional
    public Notification createForUser(String userId, String title, String message,
                                      Notification.Type type, String referenceType, String referenceId) {
        Notification n = notificationDomainService.create(userId, title, message, type, referenceType, referenceId);
        Notification saved = notificationRepository.save(n);
        safePush(saved);
        return saved;
    }

    /** Tạo cùng nội dung cho nhiều người nhận (mỗi người 1 bản ghi để theo dõi is_read riêng). */
    @Transactional
    public void createForUsers(List<String> userIds, String title, String message,
                               Notification.Type type, String referenceType, String referenceId) {
        if (userIds == null || userIds.isEmpty()) {
            log.info("[NOTIFY] Không có người nhận cho thông báo '{}' (ref {}:{}) — bỏ qua.",
                    title, referenceType, referenceId);
            return;
        }
        for (String userId : userIds) {
            Notification n = notificationDomainService.create(userId, title, message, type, referenceType, referenceId);
            safePush(notificationRepository.save(n));
        }
    }

    private void safePush(Notification n) {
        try {
            notificationPusher.push(n);
        } catch (Exception ex) {
            log.warn("[NOTIFY] Đẩy real-time thất bại (id={}): {}", n.getId(), ex.getMessage());
        }
    }
}
