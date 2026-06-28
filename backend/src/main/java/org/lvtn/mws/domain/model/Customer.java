package org.lvtn.mws.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;

/** Khách hàng. Có soft-delete (status INACTIVE + deletedAt). Thuần Java. */
public class Customer {

    public enum Status { ACTIVE, INACTIVE }

    private final String id;
    private final String code;
    private String name;
    private String taxCode;
    private String phone;
    private String email;
    private String address;
    private Status status;
    private LocalDateTime createdAt;
    private LocalDateTime deletedAt;

    private Customer(Builder b) {
        this.id        = Objects.requireNonNull(b.id, "Customer id is required");
        this.code      = Objects.requireNonNull(b.code, "Mã KH không được trống");
        this.name      = Objects.requireNonNull(b.name, "Tên KH không được trống");
        this.taxCode   = b.taxCode;
        this.phone     = b.phone;
        this.email     = b.email;
        this.address   = b.address;
        this.status    = b.status != null ? b.status : Status.ACTIVE;
        this.createdAt = b.createdAt;
        this.deletedAt = b.deletedAt;
    }

    public static class Builder {
        private String id, code, name, taxCode, phone, email, address;
        private Status status;
        private LocalDateTime createdAt, deletedAt;
        public Builder id(String v){this.id=v;return this;}
        public Builder code(String v){this.code=v;return this;}
        public Builder name(String v){this.name=v;return this;}
        public Builder taxCode(String v){this.taxCode=v;return this;}
        public Builder phone(String v){this.phone=v;return this;}
        public Builder email(String v){this.email=v;return this;}
        public Builder address(String v){this.address=v;return this;}
        public Builder status(Status v){this.status=v;return this;}
        public Builder createdAt(LocalDateTime v){this.createdAt=v;return this;}
        public Builder deletedAt(LocalDateTime v){this.deletedAt=v;return this;}
        public Customer build(){return new Customer(this);}
    }

    public void update(String name, String taxCode, String phone, String email, String address) {
        if (name != null && !name.isBlank()) this.name = name;
        this.taxCode = taxCode;
        this.phone = phone;
        this.email = email;
        this.address = address;
    }

    public void delete() {
        this.status = Status.INACTIVE;
        this.deletedAt = LocalDateTime.now();
    }

    public boolean isDeleted() { return this.deletedAt != null; }

    public String getId(){return id;}
    public String getCode(){return code;}
    public String getName(){return name;}
    public String getTaxCode(){return taxCode;}
    public String getPhone(){return phone;}
    public String getEmail(){return email;}
    public String getAddress(){return address;}
    public Status getStatus(){return status;}
    public LocalDateTime getCreatedAt(){return createdAt;}
    public LocalDateTime getDeletedAt(){return deletedAt;}
}
