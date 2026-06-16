package org.lvtn.mws.infrastructure.persistence.repository.permission;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.Permission;
import org.lvtn.mws.domain.repository.IPermissionRepository;
import org.lvtn.mws.infrastructure.persistence.mapper.PermissionInfraMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class PermissionRepositoryImpl implements IPermissionRepository {

    private final PermissionJpaRepository jpaRepository;
    private final PermissionInfraMapper   mapper;

    @Override
    public Permission save(Permission permission) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(permission)));
    }

    @Override
    public Optional<Permission> findById(String id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Permission> findAll() {
        return jpaRepository.findAll().stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Permission> findByIds(List<String> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        return jpaRepository.findByIdIn(ids).stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public boolean existsByCode(String code) { return jpaRepository.existsByCode(code); }

    @Override
    public boolean existsByCodeExcludingId(String id, String code) {
        return jpaRepository.existsByCodeAndIdNot(code, id);
    }

    @Override
    public void deleteById(String id) { jpaRepository.deleteById(id); }
}
