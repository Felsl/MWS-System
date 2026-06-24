package org.lvtn.mws.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.lvtn.mws.domain.model.Carrier;

@Entity
@Table(name = "carriers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CarrierEntity {

    @Id
    @Column(name = "id", length = 20)
    private String id;

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "shipping_fee_rule", columnDefinition = "TEXT")
    private String shippingFeeRule;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private Carrier.Status status;
}
