package org.lvtn.mws.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class Warehouse {

    private final String id;
    private String code;
    private String name;
    private String address;
    private String status;
    private final LocalDateTime createdAt;
    private LocalDateTime deletedAt;

    public static final String STATUS_ACTIVE   = "ACTIVE";
    public static final String STATUS_INACTIVE = "INACTIVE";

    private Warehouse(WarehouseBuilder builder) {
        this.id        = builder.id;
        this.code      = Objects.requireNonNull(builder.code,    "Warehouse code is required");
        this.name      = Objects.requireNonNull(builder.name,    "Warehouse name is required");
        this.address   = Objects.requireNonNull(builder.address, "Warehouse address is required");
        this.status    = builder.status != null ? builder.status : STATUS_ACTIVE;
        this.createdAt = builder.createdAt != null ? builder.createdAt : LocalDateTime.now();
        this.deletedAt = builder.deletedAt;
    }

    public static class WarehouseBuilder {
        private String id;
        private String code;
        private String name;
        private String address;
        private String status;
        private LocalDateTime createdAt;
        private LocalDateTime deletedAt;

        public WarehouseBuilder id(String id)              { this.id = id; return this; }
        public WarehouseBuilder code(String code)          { this.code = code; return this; }
        public WarehouseBuilder name(String name)          { this.name = name; return this; }
        public WarehouseBuilder address(String address)    { this.address = address; return this; }
        public WarehouseBuilder status(String status)      { this.status = status; return this; }
        public WarehouseBuilder createdAt(LocalDateTime v) { this.createdAt = v; return this; }
        public WarehouseBuilder deletedAt(LocalDateTime v) { this.deletedAt = v; return this; }

        public Warehouse build() {
            Objects.requireNonNull(code,    "Warehouse code is required");
            Objects.requireNonNull(name,    "Warehouse name is required");
            Objects.requireNonNull(address, "Warehouse address is required");
            return new Warehouse(this);
        }
    }

    public void update(String code, String name, String address) {
        Objects.requireNonNull(code,    "Warehouse code is required");
        Objects.requireNonNull(name,    "Warehouse name is required");
        Objects.requireNonNull(address, "Warehouse address is required");
        this.code    = code;
        this.name    = name;
        this.address = address;
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
        this.status    = STATUS_INACTIVE;
    }

    public boolean isDeleted() { return this.deletedAt != null; }
    public boolean isActive()  { return STATUS_ACTIVE.equals(this.status) && !isDeleted(); }

    public String getId()               { return id; }
    public String getCode()             { return code; }
    public String getName()             { return name; }
    public String getAddress()          { return address; }
    public String getStatus()           { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getDeletedAt() { return deletedAt; }
}
