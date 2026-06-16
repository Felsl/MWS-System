package org.lvtn.mws.infrastructure.persistence.repository.role;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.Role;
import org.lvtn.mws.domain.repository.IRoleRepository;
import org.lvtn.mws.infrastructure.persistence.entity.RolePermissionDbEntity;
import org.lvtn.mws.infrastructure.persistence.mapper.RoleInfraMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class RoleRepositoryImpl implements IRoleRepository {

    private final RoleJpaRepository           jpaRepository;
    private final JpaRolePermissionRepository jpaRolePermissionRepository;
    private final RoleInfraMapper             mapper;

    @Override
    @Transactional
    public Role save(Role role) {
        jpaRepository.save(mapper.toEntity(role));
        // sync permissions
        jpaRolePermissionRepository.deleteByIdRoleId(role.getId());
        List<RolePermissionDbEntity> perms = role.getPermissionIds().stream()
                .map(pid -> new RolePermissionDbEntity(
                        new RolePermissionDbEntity.RolePermissionId(role.getId(), pid)))
                .collect(Collectors.toList());
        jpaRolePermissionRepository.saveAll(perms);
        return findById(role.getId()).orElseThrow();
    }

    @Override
    public Optional<Role> findById(String id) {
        return jpaRepository.findById(id).map(e -> {
            List<String> permIds = jpaRolePermissionRepository.findByIdRoleId(id)
                    .stream().map(rp -> rp.getId().getPermissionId()).collect(Collectors.toList());
            return mapper.toDomain(e, permIds);
        });
    }

    @Override
    public Optional<Role> findByCode(String code) {
        return jpaRepository.findByCode(code).map(e -> {
            List<String> permIds = jpaRolePermissionRepository.findByIdRoleId(e.getId())
                    .stream().map(rp -> rp.getId().getPermissionId()).collect(Collectors.toList());
            return mapper.toDomain(e, permIds);
        });
    }

    @Override
    public List<Role> findAll() {
        return jpaRepository.findAll().stream().map(e -> {
            List<String> permIds = jpaRolePermissionRepository.findByIdRoleId(e.getId())
                    .stream().map(rp -> rp.getId().getPermissionId()).collect(Collectors.toList());
            return mapper.toDomain(e, permIds);
        }).collect(Collectors.toList());
    }

    @Override
    public boolean existsByCode(String code) { return jpaRepository.existsByCode(code); }

    @Override
    public boolean existsByCodeExcludingId(String id, String code) {
        return jpaRepository.existsByCodeAndIdNot(code, id);
    }

    @Override
    @Transactional
    public void deleteById(String id) {
        jpaRolePermissionRepository.deleteByIdRoleId(id);
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean isRoleInUse(String roleId) { return jpaRepository.isRoleInUse(roleId); }
}
