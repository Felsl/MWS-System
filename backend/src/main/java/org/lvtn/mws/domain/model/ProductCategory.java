package org.lvtn.mws.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class ProductCategory {

    private final String id;
    private String code;
    private String name;
    private String description;
    private LocalDateTime deletedAt;

    private ProductCategory(Builder b) {
        this.id          = Objects.requireNonNull(b.id,   "Category id is required");
        this.code        = Objects.requireNonNull(b.code, "Category code is required");
        this.name        = Objects.requireNonNull(b.name, "Category name is required");
        this.description = b.description;
        this.deletedAt   = b.deletedAt;
    }

    public static class Builder {
        private String id, code, name, description;
        private LocalDateTime deletedAt;
        public Builder id(String v)              { this.id = v; return this; }
        public Builder code(String v)            { this.code = v; return this; }
        public Builder name(String v)            { this.name = v; return this; }
        public Builder description(String v)     { this.description = v; return this; }
        public Builder deletedAt(LocalDateTime v){ this.deletedAt = v; return this; }
        public ProductCategory build() { return new ProductCategory(this); }
    }

    public void update(String code, String name, String description) {
        this.code        = Objects.requireNonNull(code, "Category code is required");
        this.name        = Objects.requireNonNull(name, "Category name is required");
        this.description = description;
    }

    public void delete() { this.deletedAt = LocalDateTime.now(); }
    public boolean isDeleted() { return deletedAt != null; }

    public String getId()               { return id; }
    public String getCode()             { return code; }
    public String getName()             { return name; }
    public String getDescription()      { return description; }
    public LocalDateTime getDeletedAt() { return deletedAt; }
}
