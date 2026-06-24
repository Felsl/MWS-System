package org.lvtn.mws.infrastructure.persistence.repository.notification;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.Notification;
import org.lvtn.mws.domain.repository.INotificationRepository;
import org.lvtn.mws.infrastructure.persistence.mapper.NotificationInfraMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class NotificationRepositoryImpl implements INotificationRepository {

    private final JpaNotificationRepository jpa;
    private final NotificationInfraMapper mapper;

    @Override
    public Notification save(Notification notification) {
        return mapper.toDomain(jpa.save(mapper.toEntity(notification)));
    }

    @Override
    public Optional<Notification> findById(String id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Notification> findInboxForUser(String userId) {
        return jpa.findInboxForUser(userId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public long countUnreadForUser(String userId) {
        return jpa.countUnreadForUser(userId);
    }

    @Override
    public int markAllReadForUser(String userId) {
        return jpa.markAllReadForUser(userId);
    }
}
