package org.lvtn.mws.infrastructure.persistence.repository.inventory;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.InventoryBatch;
import org.lvtn.mws.domain.repository.IInventoryBatchRepository;
import org.lvtn.mws.infrastructure.persistence.mapper.InventoryBatchInfraMapper;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class InventoryBatchRepositoryImpl implements IInventoryBatchRepository {

    private final JpaInventoryBatchRepository jpa;
    private final InventoryBatchInfraMapper mapper;

    @Override
    public InventoryBatch save(InventoryBatch batch) {
        return mapper.toDomain(jpa.save(mapper.toEntity(batch)));
    }

    @Override
    public Optional<InventoryBatch> findById(String id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<InventoryBatch> findByBatchNumber(String batchNumber) {
        return jpa.findByBatchNumber(batchNumber).map(mapper::toDomain);
    }

    @Override
    public List<InventoryBatch> findActiveBatchesForPicking(String productId, String warehouseId) {
        return jpa.findActiveBatchesForPicking(productId, warehouseId).stream()
                .map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<InventoryBatch> findExpiredActiveBatches(LocalDate today) {
        return jpa.findExpiredActiveBatches(today).stream()
                .map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<InventoryBatch> findByProductIdAndWarehouseId(String productId, String warehouseId) {
        return jpa.findByProductIdAndWarehouseId(productId, warehouseId).stream()
                .map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<InventoryBatch> findByWarehouseId(String warehouseId) {
        return jpa.findByWarehouseId(warehouseId).stream()
                .map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public void saveAll(List<InventoryBatch> batches) {
        jpa.saveAll(batches.stream().map(mapper::toEntity).collect(Collectors.toList()));
    }
}
