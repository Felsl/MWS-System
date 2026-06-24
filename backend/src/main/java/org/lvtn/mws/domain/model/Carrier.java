package org.lvtn.mws.domain.model;

import java.util.Objects;

/**
 * Đơn vị vận chuyển. shippingFeeRule lưu cấu hình JSON định mức tính phí ship.
 * Thuần Java — domain KHÔNG parse JSON (việc đó để cho IShippingFeeCalculator ở tầng hạ tầng).
 */
public class Carrier {

    public enum Status { ACTIVE, INACTIVE }

    private final String id;
    private final String code;
    private String name;
    private String shippingFeeRule; // chuỗi JSON thô
    private Status status;

    private Carrier(Builder b) {
        this.id              = Objects.requireNonNull(b.id, "Carrier id is required");
        this.code            = Objects.requireNonNull(b.code, "Mã đơn vị vận chuyển không được trống");
        this.name            = Objects.requireNonNull(b.name, "Tên đơn vị vận chuyển không được trống");
        this.shippingFeeRule = b.shippingFeeRule;
        this.status          = b.status != null ? b.status : Status.ACTIVE;
    }

    public static class Builder {
        private String id;
        private String code;
        private String name;
        private String shippingFeeRule;
        private Status status;

        public Builder id(String v)              { this.id = v; return this; }
        public Builder code(String v)            { this.code = v; return this; }
        public Builder name(String v)            { this.name = v; return this; }
        public Builder shippingFeeRule(String v) { this.shippingFeeRule = v; return this; }
        public Builder status(Status v)          { this.status = v; return this; }

        public Carrier build() { return new Carrier(this); }
    }

    public boolean isActive() { return this.status == Status.ACTIVE; }

    public void deactivate() { this.status = Status.INACTIVE; }

    public String getId()              { return id; }
    public String getCode()            { return code; }
    public String getName()            { return name; }
    public String getShippingFeeRule() { return shippingFeeRule; }
    public Status getStatus()          { return status; }
}
