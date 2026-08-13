package org.lvtn.mws.infrastructure.persistence.repository.salesorder;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.StockDemand;
import org.lvtn.mws.domain.repository.IStockDemandRepository;
import org.lvtn.mws.infrastructure.persistence.entity.StockDemandEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class StockDemandRepositoryImpl implements IStockDemandRepository {

    private final JpaStockDemandRepository jpa;

    @Override
    public StockDemand save(StockDemand d) {
        return toDomain(jpa.save(toEntity(d)));
    }

    @Override
    public List<StockDemand> findOpenByProductAndWarehouse(String productId, String warehouseId) {
        return jpa.findByProductIdAndWarehouseIdAndStatusOrderByCreatedAtAsc(productId, warehouseId, "OPEN")
                .stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<StockDemand> findOpenBySoId(String soId) {
        return jpa.findBySoIdAndStatus(soId, "OPEN")
                .stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<StockDemand> findAllOpen() {
        return jpa.findByStatusOrderByCreatedAtDesc("OPEN")
                .stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<StockDemand> findBySoId(String soId) {
        return jpa.findBySoId(soId).stream().map(this::toDomain).collect(Collectors.toList());
    }

    private StockDemandEntity toEntity(StockDemand d) {
        StockDemandEntity e = new StockDemandEntity();
        e.setId(d.getId());
        e.setSoId(d.getSoId());
        e.setSoDetailId(d.getSoDetailId());
        e.setProductId(d.getProductId());
        e.setWarehouseId(d.getWarehouseId());
        e.setSupplierId(d.getSupplierId());
        e.setQuantityShort(d.getQuantityShort());
        e.setStatus(d.getStatus().name());
        e.setCreatedAt(d.getCreatedAt());
        e.setUpdatedAt(d.getUpdatedAt());
        return e;
    }

    private StockDemand toDomain(StockDemandEntity e) {
        return StockDemand.builder()
                .id(e.getId())
                .soId(e.getSoId())
                .soDetailId(e.getSoDetailId())
                .productId(e.getProductId())
                .warehouseId(e.getWarehouseId())
                .supplierId(e.getSupplierId())
                .quantityShort(e.getQuantityShort())
                .status(StockDemand.Status.valueOf(e.getStatus()))
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}
