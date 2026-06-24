package org.lvtn.mws.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * [GIAI ĐOẠN 7] Thông báo rủi ro / quy trình vận hành kho — đối tượng domain thuần.
 *
 * Khớp bảng notifications: userId NULL = thông báo chung; type ∈ {ALERT, INFO, WARNING};
 * reference_type/reference_id để điều hướng tới chứng từ liên quan; is_read mặc định false.
 */
public class Notification {

    public enum Type { ALERT, INFO, WARNING }

    private final String id;
    private final String userId;        // nullable
    private final String title;
    private final String message;
    private final Type type;
    private final String referenceType; // nullable
    private final String referenceId;   // nullable
    private boolean read;
    private final LocalDateTime createdAt;

    private Notification(Builder b) {
        this.id            = Objects.requireNonNull(b.id, "Notification id is required");
        this.userId        = b.userId;
        this.title         = Objects.requireNonNull(b.title, "title is required");
        this.message       = Objects.requireNonNull(b.message, "message is required");
        this.type          = Objects.requireNonNull(b.type, "type is required");
        this.referenceType = b.referenceType;
        this.referenceId   = b.referenceId;
        this.read          = b.read;
        this.createdAt     = b.createdAt != null ? b.createdAt : LocalDateTime.now();
    }

    public static Builder builder() { return new Builder(); }

    /** Đánh dấu đã đọc (idempotent). */
    public void markRead() { this.read = true; }

    /** Kiểm tra quyền sở hữu: thông báo cá nhân chỉ chủ nhân được thao tác. */
    public boolean isOwnedBy(String requesterUserId) {
        return userId == null || userId.equals(requesterUserId);
    }

    public static class Builder {
        private String id, userId, title, message, referenceType, referenceId;
        private Type type;
        private boolean read = false;
        private LocalDateTime createdAt;

        public Builder id(String v)             { this.id = v; return this; }
        public Builder userId(String v)         { this.userId = v; return this; }
        public Builder title(String v)          { this.title = v; return this; }
        public Builder message(String v)        { this.message = v; return this; }
        public Builder type(Type v)             { this.type = v; return this; }
        public Builder referenceType(String v)  { this.referenceType = v; return this; }
        public Builder referenceId(String v)    { this.referenceId = v; return this; }
        public Builder read(boolean v)          { this.read = v; return this; }
        public Builder createdAt(LocalDateTime v){ this.createdAt = v; return this; }
        public Notification build()             { return new Notification(this); }
    }

    public String getId()             { return id; }
    public String getUserId()         { return userId; }
    public String getTitle()          { return title; }
    public String getMessage()        { return message; }
    public Type getType()             { return type; }
    public String getReferenceType()  { return referenceType; }
    public String getReferenceId()    { return referenceId; }
    public boolean isRead()           { return read; }
    public LocalDateTime getCreatedAt(){ return createdAt; }
}
