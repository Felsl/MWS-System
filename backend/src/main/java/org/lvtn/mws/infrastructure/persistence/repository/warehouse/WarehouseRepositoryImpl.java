package org.lvtn.mws.infrastructure.persistence.repository.warehouse;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.Warehouse;
import org.lvtn.mws.domain.repository.IWarehouseRepository;
import org.lvtn.mws.infrastructure.persistence.mapper.WarehouseInfraMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class WarehouseRepositoryImpl implements IWarehouseRepository {

    private final JpaWarehouseRepository jpaRepository;
    private final WarehouseInfraMapper   mapper;

    @Override
    public Warehouse save(Warehouse warehouse) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(warehouse)));
    }

    @Override
    public Optional<Warehouse> findById(String id) {
        return jpaRepository.findByIdAndDeletedAtIsNull(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Warehouse> findByCode(String code) {
        return jpaRepository.findByCodeAndDeletedAtIsNull(code).map(mapper::toDomain);
    }

    @Override
    public List<Warehouse> findAllActive() {
        return jpaRepository.findByDeletedAtIsNull()
                .stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Warehouse> findByIds(List<String> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        return jpaRepository.findByIdInAndDeletedAtIsNull(ids)
                .stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public boolean existsById(String id) {
        return jpaRepository.existsByIdAndDeletedAtIsNull(id);
    }

    @Override
    public boolean existsByCode(String code) {
        return jpaRepository.existsByCodeAndDeletedAtIsNull(code);
    }

    @Override
    public boolean existsByCodeExcludingId(String id, String code) {
        return jpaRepository.existsByCodeAndIdNotAndDeletedAtIsNull(code, id);
    }
}
