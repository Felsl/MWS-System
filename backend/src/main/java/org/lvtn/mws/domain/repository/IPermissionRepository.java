package org.lvtn.mws.domain.repository;

import org.lvtn.mws.domain.model.Permission;

import java.util.List;
import java.util.Optional;

public interface IPermissionRepository {
    Permission save(Permission permission);
    Optional<Permission> findById(String id);
    List<Permission> findAll();
    List<Permission> findByIds(List<String> ids);
    boolean existsByCode(String code);
    boolean existsByCodeExcludingId(String id, String code);
    void deleteById(String id);
}
