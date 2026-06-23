package org.lvtn.mws.infrastructure.persistence.repository.stocktake;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.StocktakeSession;
import org.lvtn.mws.domain.repository.IStocktakeSessionRepository;
import org.lvtn.mws.infrastructure.persistence.mapper.StocktakeSessionInfraMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class StocktakeSessionRepositoryImpl implements IStocktakeSessionRepository {

    private static final String FROZEN = StocktakeSession.Status.FREEZED.name();

    private final JpaStocktakeSessionRepository jpa;
    private final StocktakeSessionInfraMapper mapper;

    @Override
    public StocktakeSession save(StocktakeSession session) {
        return mapper.toDomain(jpa.save(mapper.toEntity(session)));
    }

    @Override
    public Optional<StocktakeSession> findById(String id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<StocktakeSession> findAll() {
        return jpa.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<StocktakeSession> findByWarehouseId(String warehouseId) {
        return jpa.findByWarehouseId(warehouseId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public Optional<StocktakeSession> findFrozenByWarehouseId(String warehouseId) {
        return jpa.findFirstByWarehouseIdAndStatus(warehouseId, FROZEN).map(mapper::toDomain);
    }

    @Override
    public boolean isWarehouseFrozen(String warehouseId) {
        return jpa.existsByWarehouseIdAndStatus(warehouseId, FROZEN);
    }
}
