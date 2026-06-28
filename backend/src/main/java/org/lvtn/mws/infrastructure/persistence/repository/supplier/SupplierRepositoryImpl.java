package org.lvtn.mws.infrastructure.persistence.repository.supplier;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.Supplier;
import org.lvtn.mws.domain.repository.ISupplierRepository;
import org.lvtn.mws.infrastructure.persistence.mapper.SupplierInfraMapper;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SupplierRepositoryImpl implements ISupplierRepository {

    private final JpaSupplierRepository jpa;
    private final SupplierInfraMapper mapper;

    @Override
    public Supplier save(Supplier supplier) {
        jpa.save(mapper.toEntity(supplier));
        return supplier;
    }

    @Override
    public Optional<Supplier> findById(String id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Supplier> findAll() {
        List<Supplier> result = new ArrayList<>();
        jpa.findByDeletedAtIsNull().forEach(e -> result.add(mapper.toDomain(e)));
        return result;
    }

    @Override
    public boolean existsByCode(String code) {
        return jpa.existsByCode(code);
    }
}
