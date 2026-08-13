package org.lvtn.mws.infrastructure.persistence.repository.inventory;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.InventoryBatch;
import org.lvtn.mws.domain.model.BinOccupancy;
import org.lvtn.mws.domain.repository.IInventoryBatchRepository;
import org.lvtn.mws.infrastructure.persistence.mapper.InventoryBatchInfraMapper;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
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
    public List<InventoryBatch> findActiveBatchesForPickingBySupplier(String productId, String warehouseId, String supplierId) {
        return jpa.findActiveBatchesForPickingBySupplier(productId, warehouseId, supplierId).stream()
                .map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public java.util.Map<String, Integer> sumSellableByWarehouse(String warehouseId) {
        java.util.Map<String, Integer> out = new java.util.LinkedHashMap<>();
        for (Object[] row : jpa.sumSellableByWarehouse(warehouseId)) {
            out.put((String) row[0], ((Number) row[1]).intValue());
        }
        return out;
    }

    @Override
    public List<InventoryBatch> findExpiredActiveBatches(LocalDate today) {
        return jpa.findExpiredActiveBatches(today).stream()
                .map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<InventoryBatch> findNearExpiryActiveBatches(LocalDate today, LocalDate threshold) {
        return jpa.findNearExpiryActiveBatches(today, threshold).stream()
                .map(mapper::toDomain).toList();
    }

    @Override
    public List<InventoryBatch> findExpiring(LocalDate threshold, String warehouseId) {
        return jpa.findExpiring(threshold, warehouseId).stream()
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

    @Override
    public List<BinOccupancy> sumOccupancyByWarehouse(String warehouseId) {
        List<BinOccupancy> out = new ArrayList<>();
        for (Object[] r : jpa.sumOccupancyByBin(warehouseId)) {
            out.add(new BinOccupancy((String) r[0], toBigDecimal(r[1]), toBigDecimal(r[2])));
        }
        return out;
    }

    private static BigDecimal toBigDecimal(Object o) {
        if (o == null) return BigDecimal.ZERO;
        if (o instanceof BigDecimal bd) return bd;
        if (o instanceof Number n) return new BigDecimal(n.toString());
        return new BigDecimal(o.toString());
    }
}
