package org.lvtn.mws.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "picking_lists")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class PickingListEntity {

    @Id
    @Column(length = 20)
    private String id;

    // Mã nghiệp vụ hiển thị (VD: PK-XXXXXXXX). Nullable để tương thích lệnh cũ.
    @Column(name = "pick_number", length = 50, unique = true)
    private String pickNumber;

    @Column(name = "so_id", length = 20)
    private String soId;

    @Column(name = "transfer_order_id", length = 20)
    private String transferOrderId;

    @Column(name = "assigned_to", length = 20)
    private String assignedTo;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @OneToMany(mappedBy = "pickingList", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<PickingListDetailEntity> details = new ArrayList<>();
}
