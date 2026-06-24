package org.lvtn.mws.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.lvtn.mws.domain.model.Notification.Type;

import java.time.LocalDateTime;

/** [GIAI ĐOẠN 7] Khớp bảng notifications. */
@Entity
@Table(name = "notifications")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class NotificationEntity {

    @Id
    @Column(name = "id", length = 20, nullable = false)
    private String id;

    @Column(name = "user_id", length = 20)
    private String userId;

    @Column(name = "title", length = 255, nullable = false)
    private String title;

    @Column(name = "message", columnDefinition = "TEXT", nullable = false)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 50, nullable = false)
    private Type type;

    @Column(name = "reference_type", length = 30)
    private String referenceType;

    @Column(name = "reference_id", length = 20)
    private String referenceId;

    @Column(name = "is_read")
    private boolean read;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
