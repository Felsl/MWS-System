package org.lvtn.mws.infrastructure.persistence.repository.inventory;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.Inventory;
import org.lvtn.mws.domain.repository.IInventoryRepository;
import org.lvtn.mws.infrastructure.persistence.entity.InventoryId;
import org.lvtn.mws.infrastructure.persistence.mapper.InventoryInfraMapper;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class InventoryRepositoryImpl implements IInventoryRepository {

    private final JpaInventoryRepository jpa;
    private final InventoryInfraMapper mapper;

    @Override
    public Inventory save(Inventory inventory) {
        return mapper.toDomain(jpa.save(mapper.toEntity(inventory)));
    }

    @Override
    public Optional<Inventory> findByProductIdAndWarehouseId(String productId, String warehouseId) {
        return jpa.findById(new InventoryId(productId, warehouseId)).map(mapper::toDomain);
    }

    @Override
    public List<Inventory> findByWarehouseId(String warehouseId) {
        return jpa.findByIdWarehouseId(warehouseId).stream()
                .map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Inventory> findByProductId(String productId) {
        return jpa.findByIdProductId(productId).stream()
                .map(mapper::toDomain).collect(Collectors.toList());
    }
}
