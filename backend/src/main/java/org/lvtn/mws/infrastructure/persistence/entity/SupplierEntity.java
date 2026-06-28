package org.lvtn.mws.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.lvtn.mws.domain.model.Supplier;

import java.time.LocalDateTime;

@Entity
@Table(name = "suppliers")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class SupplierEntity {

    @Id
    @Column(name = "id", length = 20)
    private String id;

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "contact_name", length = 100)
    private String contactName;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private Supplier.Status status;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
