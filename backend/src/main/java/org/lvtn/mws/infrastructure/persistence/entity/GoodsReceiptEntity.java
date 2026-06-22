package org.lvtn.mws.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.lvtn.mws.domain.model.GoodsReceipt.Status;

import java.time.LocalDateTime;

@Entity
@Table(name = "goods_receipts")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class GoodsReceiptEntity {

    @Id
    @Column(name = "id", length = 20, nullable = false)
    private String id;

    @Column(name = "grn_number", length = 50, nullable = false, unique = true)
    private String grnNumber;

    @Column(name = "po_id", length = 20)
    private String poId;

    @Column(name = "warehouse_id", length = 20, nullable = false)
    private String warehouseId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30, nullable = false)
    private Status status;

    @Column(name = "received_by", length = 50, nullable = false)
    private String receivedBy;

    @Column(name = "received_at")
    private LocalDateTime receivedAt;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;
}
