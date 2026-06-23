package org.lvtn.mws.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "adjustment_vouchers")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class AdjustmentVoucherEntity {

    @Id
    @Column(name = "id", length = 20, nullable = false)
    private String id;

    @Column(name = "voucher_number", length = 50, nullable = false, unique = true)
    private String voucherNumber;

    @Column(name = "warehouse_id", length = 20, nullable = false)
    private String warehouseId;

    @Column(name = "session_id", length = 20)
    private String sessionId;

    @Column(name = "reason", length = 100, nullable = false)
    private String reason;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "created_by", length = 20)
    private String createdBy;

    @Column(name = "approved_by", length = 20)
    private String approvedBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "voucher", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<AdjustmentVoucherDetailEntity> details = new ArrayList<>();
}
