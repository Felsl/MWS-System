package org.lvtn.mws.infrastructure.persistence.mapper;

import org.lvtn.mws.domain.model.Notification;
import org.lvtn.mws.infrastructure.persistence.entity.NotificationEntity;
import org.mapstruct.Mapper;

/** [GIAI ĐOẠN 7] Mapper viết tay (default) cho Notification (immutable + builder). */
@Mapper(componentModel = "spring")
public interface NotificationInfraMapper {

    default NotificationEntity toEntity(Notification n) {
        if (n == null) return null;
        NotificationEntity e = new NotificationEntity();
        e.setId(n.getId());
        e.setUserId(n.getUserId());
        e.setTitle(n.getTitle());
        e.setMessage(n.getMessage());
        e.setType(n.getType());
        e.setReferenceType(n.getReferenceType());
        e.setReferenceId(n.getReferenceId());
        e.setRead(n.isRead());
        e.setCreatedAt(n.getCreatedAt());
        return e;
    }

    default Notification toDomain(NotificationEntity e) {
        if (e == null) return null;
        return Notification.builder()
                .id(e.getId())
                .userId(e.getUserId())
                .title(e.getTitle())
                .message(e.getMessage())
                .type(e.getType())
                .referenceType(e.getReferenceType())
                .referenceId(e.getReferenceId())
                .read(e.isRead())
                .createdAt(e.getCreatedAt())
                .build();
    }
}
