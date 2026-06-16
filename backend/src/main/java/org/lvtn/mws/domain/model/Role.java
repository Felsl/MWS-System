package org.lvtn.mws.domain.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Role {

    private final String id;
    private String code;
    private String name;
    private String description;
    private List<String> permissionIds;

    private Role(RoleBuilder builder) {
        this.id            = builder.id;
        this.code          = Objects.requireNonNull(builder.code, "Role code is required");
        this.name          = Objects.requireNonNull(builder.name, "Role name is required");
        this.description   = builder.description;
        this.permissionIds = builder.permissionIds != null ? builder.permissionIds : new ArrayList<>();
    }

    public static class RoleBuilder {
        private String id;
        private String code;
        private String name;
        private String description;
        private List<String> permissionIds;

        public RoleBuilder id(String id)                       { this.id = id; return this; }
        public RoleBuilder code(String code)                   { this.code = code; return this; }
        public RoleBuilder name(String name)                   { this.name = name; return this; }
        public RoleBuilder description(String description)     { this.description = description; return this; }
        public RoleBuilder permissionIds(List<String> ids)     { this.permissionIds = ids; return this; }

        public Role build() {
            Objects.requireNonNull(code, "Role code is required");
            Objects.requireNonNull(name, "Role name is required");
            return new Role(this);
        }
    }

    public void update(String code, String name, String description) {
        Objects.requireNonNull(code, "Role code is required");
        Objects.requireNonNull(name, "Role name is required");
        this.code        = code;
        this.name        = name;
        this.description = description;
    }

    public void assignPermissions(List<String> permissionIds) {
        this.permissionIds = permissionIds != null ? permissionIds : new ArrayList<>();
    }

    public void addPermission(String permissionId) {
        Objects.requireNonNull(permissionId, "Permission id is required");
        if (!this.permissionIds.contains(permissionId)) {
            this.permissionIds.add(permissionId);
        }
    }

    public void removePermission(String permissionId) {
        this.permissionIds.remove(permissionId);
    }

    public String getId()                    { return id; }
    public String getCode()                  { return code; }
    public String getName()                  { return name; }
    public String getDescription()           { return description; }
    public List<String> getPermissionIds()   { return permissionIds; }
}
