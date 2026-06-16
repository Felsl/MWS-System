package org.lvtn.mws.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class User {

    private final String id;
    private String username;
    private String password;
    private String fullName;
    private String email;
    private String phone;
    private String status;
    private String roleId;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    public static final String STATUS_ACTIVE   = "ACTIVE";
    public static final String STATUS_INACTIVE = "INACTIVE";

    private User(UserBuilder builder) {
        this.id        = builder.id;
        this.username  = Objects.requireNonNull(builder.username,  "Username is required");
        this.password  = Objects.requireNonNull(builder.password,  "Password is required");
        this.fullName  = Objects.requireNonNull(builder.fullName,  "Full name is required");
        this.email     = builder.email;
        this.phone     = builder.phone;
        this.status    = builder.status != null ? builder.status : STATUS_ACTIVE;
        this.roleId    = Objects.requireNonNull(builder.roleId,    "Role is required");
        this.createdAt = builder.createdAt != null ? builder.createdAt : LocalDateTime.now();
        this.updatedAt = builder.updatedAt != null ? builder.updatedAt : this.createdAt;
        this.deletedAt = builder.deletedAt;
    }

    public static class UserBuilder {
        private String id;
        private String username;
        private String password;
        private String fullName;
        private String email;
        private String phone;
        private String status;
        private String roleId;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private LocalDateTime deletedAt;

        public UserBuilder id(String id)              { this.id = id; return this; }
        public UserBuilder username(String v)         { this.username = v; return this; }
        public UserBuilder password(String v)         { this.password = v; return this; }
        public UserBuilder fullName(String v)         { this.fullName = v; return this; }
        public UserBuilder email(String v)            { this.email = v; return this; }
        public UserBuilder phone(String v)            { this.phone = v; return this; }
        public UserBuilder status(String v)           { this.status = v; return this; }
        public UserBuilder roleId(String v)           { this.roleId = v; return this; }
        public UserBuilder createdAt(LocalDateTime v) { this.createdAt = v; return this; }
        public UserBuilder updatedAt(LocalDateTime v) { this.updatedAt = v; return this; }
        public UserBuilder deletedAt(LocalDateTime v) { this.deletedAt = v; return this; }

        public User build() {
            Objects.requireNonNull(username, "Username is required");
            Objects.requireNonNull(password, "Password is required");
            Objects.requireNonNull(fullName, "Full name is required");
            Objects.requireNonNull(roleId,   "Role is required");
            return new User(this);
        }
    }

    public void update(String fullName, String email, String phone, String roleId) {
        Objects.requireNonNull(fullName, "Full name is required");
        Objects.requireNonNull(roleId,   "Role is required");
        this.fullName  = fullName;
        this.email     = email;
        this.phone     = phone;
        this.roleId    = roleId;
        this.updatedAt = LocalDateTime.now();
    }

    public void changePassword(String encodedPassword) {
        Objects.requireNonNull(encodedPassword, "Password is required");
        this.password  = encodedPassword;
        this.updatedAt = LocalDateTime.now();
    }

    public void deactivate() {
        this.status    = STATUS_INACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
        this.status    = STATUS_INACTIVE;
    }

    public boolean isDeleted() { return this.deletedAt != null; }
    public boolean isActive()  { return STATUS_ACTIVE.equals(this.status) && !isDeleted(); }

    public String getId()               { return id; }
    public String getUsername()         { return username; }
    public String getPassword()         { return password; }
    public String getFullName()         { return fullName; }
    public String getEmail()            { return email; }
    public String getPhone()            { return phone; }
    public String getStatus()           { return status; }
    public String getRoleId()           { return roleId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public LocalDateTime getDeletedAt() { return deletedAt; }
}
