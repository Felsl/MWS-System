package org.lvtn.mws.domain.service;

import org.lvtn.mws.domain.model.Permission;
import org.lvtn.mws.domain.model.Role;
import org.lvtn.mws.domain.repository.IPermissionRepository;
import org.lvtn.mws.domain.repository.IRoleRepository;

import java.util.List;
import java.util.stream.Collectors;

public class RoleDomainService {

    private final IRoleRepository roleRepository;
    private final IPermissionRepository permissionRepository;

    public RoleDomainService(IRoleRepository roleRepository,
                             IPermissionRepository permissionRepository) {
        this.roleRepository       = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    public Role create(String id, String code, String name, String description) {
        validateCodeNotTaken(code);
        Role role = new Role.RoleBuilder()
                .id(id)
                .code(code)
                .name(name)
                .description(description)
                .build();
        return roleRepository.save(role);
    }

    public Role findById(String id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found: " + id));
    }

    public List<Role> findAll() {
        return roleRepository.findAll();
    }

    public Role update(String id, String code, String name, String description) {
        Role role = findById(id);
        if (!role.getCode().equals(code)) {
            validateCodeNotTakenExcluding(id, code);
        }
        role.update(code, name, description);
        return roleRepository.save(role);
    }

    public void delete(String id) {
        findById(id);
        if (roleRepository.isRoleInUse(id)) {
            throw new IllegalStateException("Cannot delete role: it is currently assigned to users");
        }
        roleRepository.deleteById(id);
    }

    public Role assignPermissions(String roleId, List<String> permissionIds) {
        Role role = findById(roleId);
        validatePermissionsExist(permissionIds);
        role.assignPermissions(permissionIds);
        return roleRepository.save(role);
    }

    public List<Permission> getPermissionsForRole(String roleId) {
        Role role = findById(roleId);
        return permissionRepository.findByIds(role.getPermissionIds());
    }

    private void validateCodeNotTaken(String code) {
        if (roleRepository.existsByCode(code)) {
            throw new IllegalArgumentException("Role code already exists: " + code);
        }
    }

    private void validateCodeNotTakenExcluding(String id, String code) {
        if (roleRepository.existsByCodeExcludingId(id, code)) {
            throw new IllegalArgumentException("Role code already exists: " + code);
        }
    }

    private void validatePermissionsExist(List<String> permissionIds) {
        if (permissionIds == null || permissionIds.isEmpty()) return;
        List<String> found = permissionRepository.findByIds(permissionIds)
                .stream().map(Permission::getId).collect(Collectors.toList());
        permissionIds.forEach(pid -> {
            if (!found.contains(pid)) {
                throw new RuntimeException("Permission not found: " + pid);
            }
        });
    }
}
