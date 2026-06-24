package org.lvtn.mws.infrastructure.persistence.repository.stockmovement;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.StockMovement;
import org.lvtn.mws.domain.repository.IStockMovementRepository;
import org.lvtn.mws.infrastructure.persistence.mapper.StockMovementInfraMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class StockMovementRepositoryImpl implements IStockMovementRepository {

    private final JpaStockMovementRepository jpa;
    private final StockMovementInfraMapper mapper;

    @Override
    public StockMovement save(StockMovement movement) {
        return mapper.toDomain(jpa.save(mapper.toEntity(movement)));
    }

    @Override
    public List<StockMovement> findByReference(String referenceType, String referenceId) {
        return jpa.findByReferenceTypeAndReferenceId(referenceType, referenceId)
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<StockMovement> findByProductId(String productId) {
        return jpa.findByProductIdOrderByCreatedAtDesc(productId)
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<StockMovement> findByProductIdAndWarehouseId(String productId, String warehouseId) {
        return jpa.findByProductIdAndWarehouseIdOrderByCreatedAtDesc(productId, warehouseId)
                .stream().map(mapper::toDomain).toList();
    }
}
