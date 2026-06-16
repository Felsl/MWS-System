package org.lvtn.mws.domain.service;

import org.lvtn.mws.domain.model.Permission;
import org.lvtn.mws.domain.repository.IPermissionRepository;

import java.util.List;

public class PermissionDomainService {

    private final IPermissionRepository permissionRepository;

    public PermissionDomainService(IPermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    public Permission create(String id, String code, String name, String module) {
        if (permissionRepository.existsByCode(code)) {
            throw new IllegalArgumentException("Permission code already exists: " + code);
        }
        Permission permission = new Permission.PermissionBuilder()
                .id(id).code(code).name(name).module(module).build();
        return permissionRepository.save(permission);
    }

    public Permission findById(String id) {
        return permissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Permission not found: " + id));
    }

    public List<Permission> findAll() {
        return permissionRepository.findAll();
    }

    public Permission update(String id, String code, String name, String module) {
        Permission permission = findById(id);
        if (!permission.getCode().equals(code) && permissionRepository.existsByCodeExcludingId(id, code)) {
            throw new IllegalArgumentException("Permission code already exists: " + code);
        }
        permission.update(code, name, module);
        return permissionRepository.save(permission);
    }

    public void delete(String id) {
        findById(id);
        permissionRepository.deleteById(id);
    }
}
