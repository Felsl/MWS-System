package org.lvtn.mws.interfaces.dto.response;

import java.time.LocalDateTime;

/** [GIAI ĐOẠN 7] DTO thông báo trả về cho client. */
public record NotificationResponse(
        String id,
        String userId,
        String title,
        String message,
        String type,
        String referenceType,
        String referenceId,
        boolean isRead,
        LocalDateTime createdAt) {
}
