package org.lvtn.mws.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "permissions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class PermissionDbEntity {

    @Id
    @Column(length = 20)
    private String id;

    @Column(name = "code", nullable = false, unique = true, length = 100)
    private String code;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "module", nullable = false, length = 50)
    private String module;
}
