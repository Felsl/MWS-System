package org.lvtn.mws.domain.service;

import org.lvtn.mws.domain.model.Notification;
import org.lvtn.mws.domain.repository.IIdGenerator;

/**
 * [GIAI ĐOẠN 7] Domain service THUẦN (không annotation Spring) — chứa quy tắc dựng thông báo.
 * Đăng ký bean qua DomainServiceConfig. Không tự persist / không tự push (đó là việc của UseCase).
 */
public class NotificationDomainService {

    private final IIdGenerator idGenerator;

    public NotificationDomainService(IIdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    /** Dựng một thông báo mới (chưa đọc). userId = null nghĩa là thông báo chung toàn kho. */
    public Notification create(String userId, String title, String message,
                               Notification.Type type, String referenceType, String referenceId) {
        return Notification.builder()
                .id(idGenerator.generate())
                .userId(userId)
                .title(title)
                .message(message)
                .type(type)
                .referenceType(referenceType)
                .referenceId(referenceId)
                .read(false)
                .build();
    }

    /** Guard nghiệp vụ: chỉ chủ nhân (hoặc thông báo chung) mới được đánh dấu đã đọc. */
    public void assertCanRead(Notification notification, String requesterUserId) {
        if (!notification.isOwnedBy(requesterUserId)) {
            throw new IllegalStateException("Không có quyền thao tác thông báo của người dùng khác");
        }
    }
}
