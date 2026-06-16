package org.lvtn.mws.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "bin_locations",
       uniqueConstraints = @UniqueConstraint(
           name = "uq_location",
           columnNames = {"warehouse_id", "zone", "aisle", "rack", "bin"}
       ))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class BinLocationDbEntity {

    @Id
    @Column(length = 20)
    private String id;

    @Column(name = "warehouse_id", nullable = false, length = 20)
    private String warehouseId;

    @Column(name = "zone", nullable = false, length = 50)
    private String zone;

    @Column(name = "aisle", nullable = false, length = 50)
    private String aisle;

    @Column(name = "rack", nullable = false, length = 50)
    private String rack;

    @Column(name = "bin", nullable = false, length = 50)
    private String bin;
}
