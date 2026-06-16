package org.lvtn.mws.domain.repository;

import org.lvtn.mws.domain.model.Role;

import java.util.List;
import java.util.Optional;

public interface IRoleRepository {
    Role save(Role role);
    Optional<Role> findById(String id);
    Optional<Role> findByCode(String code);
    List<Role> findAll();
    boolean existsByCode(String code);
    boolean existsByCodeExcludingId(String id, String code);
    void deleteById(String id);
    boolean isRoleInUse(String roleId);
}
