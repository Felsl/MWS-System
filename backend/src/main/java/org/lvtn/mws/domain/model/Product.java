package org.lvtn.mws.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public class Product {

    public enum Unit { PCS, BOX, KG, BAG, BOTTLE, PALLET, SET, PAIR }

    private final String id;
    private String categoryId;
    private String sku;
    private String barcode;
    private String name;
    private String description;
    private BigDecimal price;
    private BigDecimal costPrice;
    private Unit unit;
    private int safetyStock;
    private BigDecimal weight;
    private BigDecimal volume;
    private boolean hazardousFlag;
    private int version;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    private Product(Builder b) {
        this.id           = Objects.requireNonNull(b.id,  "Product id is required");
        this.categoryId   = b.categoryId;
        this.sku          = Objects.requireNonNull(b.sku, "SKU is required");
        this.barcode      = b.barcode;
        this.name         = Objects.requireNonNull(b.name, "Product name is required");
        this.description  = b.description;
        this.price        = b.price != null ? b.price : BigDecimal.ZERO;
        this.costPrice    = b.costPrice != null ? b.costPrice : BigDecimal.ZERO;
        this.unit         = Objects.requireNonNull(b.unit, "Unit is required");
        this.safetyStock  = b.safetyStock;
        this.weight       = b.weight;
        this.volume       = b.volume;
        this.hazardousFlag = b.hazardousFlag;
        this.version      = b.version;
        this.createdAt    = b.createdAt != null ? b.createdAt : LocalDateTime.now();
        this.updatedAt    = b.updatedAt != null ? b.updatedAt : this.createdAt;
        this.deletedAt    = b.deletedAt;
    }

    public static class Builder {
        private String id, categoryId, sku, barcode, name, description;
        private BigDecimal price, costPrice, weight, volume;
        private Unit unit;
        private int safetyStock = 10;
        private boolean hazardousFlag = false;
        private int version = 0;
        private LocalDateTime createdAt, updatedAt, deletedAt;

        public Builder id(String v)              { this.id = v; return this; }
        public Builder categoryId(String v)      { this.categoryId = v; return this; }
        public Builder sku(String v)             { this.sku = v; return this; }
        public Builder barcode(String v)         { this.barcode = v; return this; }
        public Builder name(String v)            { this.name = v; return this; }
        public Builder description(String v)     { this.description = v; return this; }
        public Builder price(BigDecimal v)       { this.price = v; return this; }
        public Builder costPrice(BigDecimal v)   { this.costPrice = v; return this; }
        public Builder unit(Unit v)              { this.unit = v; return this; }
        public Builder safetyStock(int v)        { this.safetyStock = v; return this; }
        public Builder weight(BigDecimal v)      { this.weight = v; return this; }
        public Builder volume(BigDecimal v)      { this.volume = v; return this; }
        public Builder hazardousFlag(boolean v)  { this.hazardousFlag = v; return this; }
        public Builder version(int v)            { this.version = v; return this; }
        public Builder createdAt(LocalDateTime v){ this.createdAt = v; return this; }
        public Builder updatedAt(LocalDateTime v){ this.updatedAt = v; return this; }
        public Builder deletedAt(LocalDateTime v){ this.deletedAt = v; return this; }
        public Product build() { return new Product(this); }
    }

    public void update(String categoryId, String sku, String barcode, String name,
                       String description, BigDecimal price, BigDecimal costPrice,
                       Unit unit, int safetyStock, BigDecimal weight, BigDecimal volume,
                       boolean hazardousFlag) {
        this.categoryId    = categoryId;
        this.sku           = Objects.requireNonNull(sku,  "SKU is required");
        this.barcode       = barcode;
        this.name          = Objects.requireNonNull(name, "Product name is required");
        this.description   = description;
        this.price         = price != null ? price : BigDecimal.ZERO;
        this.costPrice     = costPrice != null ? costPrice : BigDecimal.ZERO;
        this.unit          = Objects.requireNonNull(unit, "Unit is required");
        this.safetyStock   = safetyStock;
        this.weight        = weight;
        this.volume        = volume;
        this.hazardousFlag = hazardousFlag;
        this.updatedAt     = LocalDateTime.now();
    }

    public void delete() { this.deletedAt = LocalDateTime.now(); }
    public boolean isDeleted() { return deletedAt != null; }

    public String getId()               { return id; }
    public String getCategoryId()       { return categoryId; }
    public String getSku()              { return sku; }
    public String getBarcode()          { return barcode; }
    public String getName()             { return name; }
    public String getDescription()      { return description; }
    public BigDecimal getPrice()        { return price; }
    public BigDecimal getCostPrice()    { return costPrice; }
    public Unit getUnit()               { return unit; }
    public int getSafetyStock()         { return safetyStock; }
    public BigDecimal getWeight()       { return weight; }
    public BigDecimal getVolume()       { return volume; }
    public boolean isHazardousFlag()    { return hazardousFlag; }
    public int getVersion()             { return version; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public LocalDateTime getDeletedAt() { return deletedAt; }
}
