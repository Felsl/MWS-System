package org.lvtn.mws.interfaces.mapper;

import org.lvtn.mws.domain.model.Notification;
import org.lvtn.mws.interfaces.dto.response.NotificationResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface NotificationWebMapper {

    default NotificationResponse toResponse(Notification n) {
        if (n == null) return null;
        return new NotificationResponse(
                n.getId(), n.getUserId(), n.getTitle(), n.getMessage(),
                n.getType() == null ? null : n.getType().name(),
                n.getReferenceType(), n.getReferenceId(), n.isRead(), n.getCreatedAt());
    }

    default List<NotificationResponse> toResponseList(List<Notification> list) {
        if (list == null) return List.of();
        return list.stream().map(this::toResponse).toList();
    }
}
