package org.lvtn.mws.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;

/** Nhà cung cấp. Có soft-delete (status INACTIVE + deletedAt). Thuần Java. */
public class Supplier {

    public enum Status { ACTIVE, INACTIVE }

    private final String id;
    private final String code;
    private String name;
    private String contactName;
    private String phone;
    private String email;
    private String address;
    private Status status;
    private LocalDateTime deletedAt;

    private Supplier(Builder b) {
        this.id          = Objects.requireNonNull(b.id, "Supplier id is required");
        this.code        = Objects.requireNonNull(b.code, "Mã NCC không được trống");
        this.name        = Objects.requireNonNull(b.name, "Tên NCC không được trống");
        this.contactName = b.contactName;
        this.phone       = b.phone;
        this.email       = b.email;
        this.address     = b.address;
        this.status      = b.status != null ? b.status : Status.ACTIVE;
        this.deletedAt   = b.deletedAt;
    }

    public static class Builder {
        private String id, code, name, contactName, phone, email, address;
        private Status status;
        private LocalDateTime deletedAt;
        public Builder id(String v){this.id=v;return this;}
        public Builder code(String v){this.code=v;return this;}
        public Builder name(String v){this.name=v;return this;}
        public Builder contactName(String v){this.contactName=v;return this;}
        public Builder phone(String v){this.phone=v;return this;}
        public Builder email(String v){this.email=v;return this;}
        public Builder address(String v){this.address=v;return this;}
        public Builder status(Status v){this.status=v;return this;}
        public Builder deletedAt(LocalDateTime v){this.deletedAt=v;return this;}
        public Supplier build(){return new Supplier(this);}
    }

    public void update(String name, String contactName, String phone, String email, String address) {
        if (name != null && !name.isBlank()) this.name = name;
        this.contactName = contactName;
        this.phone = phone;
        this.email = email;
        this.address = address;
    }

    /** Soft delete. */
    public void delete() {
        this.status = Status.INACTIVE;
        this.deletedAt = LocalDateTime.now();
    }

    public boolean isDeleted() { return this.deletedAt != null; }

    public String getId(){return id;}
    public String getCode(){return code;}
    public String getName(){return name;}
    public String getContactName(){return contactName;}
    public String getPhone(){return phone;}
    public String getEmail(){return email;}
    public String getAddress(){return address;}
    public Status getStatus(){return status;}
    public LocalDateTime getDeletedAt(){return deletedAt;}
}
