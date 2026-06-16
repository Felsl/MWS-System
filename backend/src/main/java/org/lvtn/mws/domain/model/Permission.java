package org.lvtn.mws.domain.model;

import java.util.Objects;

public class Permission {

    private final String id;
    private String code;
    private String name;
    private String module;

    private Permission(PermissionBuilder builder) {
        this.id     = builder.id;
        this.code   = Objects.requireNonNull(builder.code,   "Permission code is required");
        this.name   = Objects.requireNonNull(builder.name,   "Permission name is required");
        this.module = Objects.requireNonNull(builder.module, "Permission module is required");
    }

    public static class PermissionBuilder {
        private String id;
        private String code;
        private String name;
        private String module;

        public PermissionBuilder id(String id)         { this.id = id; return this; }
        public PermissionBuilder code(String code)     { this.code = code; return this; }
        public PermissionBuilder name(String name)     { this.name = name; return this; }
        public PermissionBuilder module(String module) { this.module = module; return this; }

        public Permission build() {
            Objects.requireNonNull(code,   "Permission code is required");
            Objects.requireNonNull(name,   "Permission name is required");
            Objects.requireNonNull(module, "Permission module is required");
            return new Permission(this);
        }
    }

    public void update(String code, String name, String module) {
        Objects.requireNonNull(code,   "Permission code is required");
        Objects.requireNonNull(name,   "Permission name is required");
        Objects.requireNonNull(module, "Permission module is required");
        this.code   = code;
        this.name   = name;
        this.module = module;
    }

    public String getId()     { return id; }
    public String getCode()   { return code; }
    public String getName()   { return name; }
    public String getModule() { return module; }
}
