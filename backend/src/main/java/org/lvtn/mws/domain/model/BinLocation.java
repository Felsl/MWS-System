package org.lvtn.mws.domain.model;

import java.util.Objects;

public class BinLocation {

    private final String id;
    private final String warehouseId;
    private String zone;
    private String aisle;
    private String rack;
    private String bin;

    private BinLocation(BinLocationBuilder builder) {
        this.id          = builder.id;
        this.warehouseId = Objects.requireNonNull(builder.warehouseId, "Warehouse is required");
        this.zone        = Objects.requireNonNull(builder.zone,        "Zone is required");
        this.aisle       = Objects.requireNonNull(builder.aisle,       "Aisle is required");
        this.rack        = Objects.requireNonNull(builder.rack,        "Rack is required");
        this.bin         = Objects.requireNonNull(builder.bin,         "Bin is required");
    }

    public static class BinLocationBuilder {
        private String id;
        private String warehouseId;
        private String zone;
        private String aisle;
        private String rack;
        private String bin;

        public BinLocationBuilder id(String id)                  { this.id = id; return this; }
        public BinLocationBuilder warehouseId(String warehouseId){ this.warehouseId = warehouseId; return this; }
        public BinLocationBuilder zone(String zone)              { this.zone = zone; return this; }
        public BinLocationBuilder aisle(String aisle)            { this.aisle = aisle; return this; }
        public BinLocationBuilder rack(String rack)              { this.rack = rack; return this; }
        public BinLocationBuilder bin(String bin)                { this.bin = bin; return this; }

        public BinLocation build() {
            Objects.requireNonNull(warehouseId, "Warehouse is required");
            Objects.requireNonNull(zone,        "Zone is required");
            Objects.requireNonNull(aisle,       "Aisle is required");
            Objects.requireNonNull(rack,        "Rack is required");
            Objects.requireNonNull(bin,         "Bin is required");
            return new BinLocation(this);
        }
    }

    /**
     * Composite key dùng để kiểm tra trùng tọa độ:
     * warehouse_id + zone + aisle + rack + bin
     */
    public String toCoordinateKey() {
        return warehouseId + "|" + zone + "|" + aisle + "|" + rack + "|" + bin;
    }

    public String getId()          { return id; }
    public String getWarehouseId() { return warehouseId; }
    public String getZone()        { return zone; }
    public String getAisle()       { return aisle; }
    public String getRack()        { return rack; }
    public String getBin()         { return bin; }
}
