package org.lvtn.mws.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "role_permissions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class RolePermissionDbEntity {

    @EmbeddedId
    private RolePermissionId id;

    @Embeddable
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    @EqualsAndHashCode
    public static class RolePermissionId implements java.io.Serializable {
        @Column(name = "role_id", length = 20)
        private String roleId;

        @Column(name = "permission_id", length = 20)
        private String permissionId;
    }
}
