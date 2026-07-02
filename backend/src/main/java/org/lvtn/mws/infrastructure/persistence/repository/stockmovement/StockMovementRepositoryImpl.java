package org.lvtn.mws.infrastructure.persistence.repository.stockmovement;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.StockMovement;
import org.lvtn.mws.domain.repository.IStockMovementRepository;
import org.lvtn.mws.infrastructure.persistence.entity.StockMovementEntity;
import org.lvtn.mws.infrastructure.persistence.mapper.StockMovementInfraMapper;
import org.lvtn.mws.infrastructure.security.scope.WarehouseScopeSpecs;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.*;

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
    public void appendAll(List<StockMovement> movements) {
        if (movements == null || movements.isEmpty()) return;
        jpa.saveAll(movements.stream().map(mapper::toEntity).toList());
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
    public List<StockMovement> findByProductIdScoped(String productId) {
        Specification<StockMovementEntity> byProduct =
                (root, q, cb) -> cb.equal(root.get("productId"), productId);
        Specification<StockMovementEntity> scoped =
                WarehouseScopeSpecs.restrict("warehouseId");
        Specification<StockMovementEntity> spec = byProduct.and(scoped);
        return jpa.findAll(spec, Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public org.lvtn.mws.domain.common.CursorPage<StockMovement> findByProductScrolling(
            String productId, String warehouseId, String cursor, int size) {
        Specification<StockMovementEntity> spec =
                ((Specification<StockMovementEntity>)
                        (root, q, cb) -> cb.equal(root.get("productId"), productId))
                        .and(WarehouseScopeSpecs.restrict("warehouseId"));
        if (warehouseId != null && !warehouseId.isBlank()) {
            String wh = warehouseId.trim();
            spec = spec.and((root, q, cb) -> cb.equal(root.get("warehouseId"), wh));
        }
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by(Sort.Direction.DESC, "id"));
        ScrollPosition position = decodeCursor(cursor);
        Window<StockMovementEntity> window =
                jpa.findBy(spec, q -> q.sortBy(sort).limit(size).scroll(position));
        List<StockMovement> content = window.stream().map(mapper::toDomain).toList();
        String nextCursor = null;
        if (window.hasNext() && !window.getContent().isEmpty()) {
            StockMovementEntity last = window.getContent().get(window.getContent().size() - 1);
            nextCursor = encodeCursor(last.getCreatedAt(), last.getId());
        }
        return new org.lvtn.mws.domain.common.CursorPage<>(content, nextCursor, window.hasNext());
    }

    private ScrollPosition decodeCursor(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return ScrollPosition.keyset();
        }
        String decoded = new String(java.util.Base64.getUrlDecoder().decode(cursor),
                java.nio.charset.StandardCharsets.UTF_8);
        int sep = decoded.indexOf('|');
        java.time.LocalDateTime createdAt = java.time.LocalDateTime.parse(decoded.substring(0, sep));
        String id = decoded.substring(sep + 1);
        java.util.LinkedHashMap<String, Object> keys = new java.util.LinkedHashMap<>();
        keys.put("createdAt", createdAt);
        keys.put("id", id);
        return ScrollPosition.forward(keys);
    }

    private String encodeCursor(java.time.LocalDateTime createdAt, String id) {
        String raw = createdAt.toString() + "|" + id;
        return java.util.Base64.getUrlEncoder().withoutPadding()
                .encodeToString(raw.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    @Override
    public List<StockMovement> findByProductIdAndWarehouseId(String productId, String warehouseId) {
        return jpa.findByProductIdAndWarehouseIdOrderByCreatedAtDesc(productId, warehouseId)
                .stream().map(mapper::toDomain).toList();
    }
}
