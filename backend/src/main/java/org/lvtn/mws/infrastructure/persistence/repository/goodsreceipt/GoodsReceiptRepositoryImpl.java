package org.lvtn.mws.infrastructure.persistence.repository.goodsreceipt;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.GoodsReceipt;
import org.lvtn.mws.domain.repository.IGoodsReceiptRepository;
import org.lvtn.mws.infrastructure.persistence.mapper.GoodsReceiptInfraMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class GoodsReceiptRepositoryImpl implements IGoodsReceiptRepository {

    private final JpaGoodsReceiptRepository jpa;
    private final GoodsReceiptInfraMapper mapper;

    @Override
    public GoodsReceipt save(GoodsReceipt goodsReceipt) {
        return mapper.toDomain(jpa.save(mapper.toEntity(goodsReceipt)));
    }

    @Override
    public Optional<GoodsReceipt> findById(String id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<GoodsReceipt> findByGrnNumber(String grnNumber) {
        return jpa.findByGrnNumber(grnNumber).map(mapper::toDomain);
    }

    @Override
    public List<GoodsReceipt> findByPoId(String poId) {
        return mapper.toDomainList(jpa.findByPoId(poId));
    }

    @Override
    public org.lvtn.mws.domain.common.PageResult<GoodsReceipt> search(
            String keyword, String status, org.lvtn.mws.domain.common.PageQuery pageQuery) {
        org.springframework.data.jpa.domain.Specification<org.lvtn.mws.infrastructure.persistence.entity.GoodsReceiptEntity> spec =
                org.lvtn.mws.infrastructure.security.scope.WarehouseScopeSpecs.restrict("warehouseId");
        if (keyword != null && !keyword.isBlank()) {
            String like = "%" + keyword.trim().toLowerCase() + "%";
            spec = spec.and((root, q, cb) -> cb.like(cb.lower(root.get("grnNumber")), like));
        }
        if (status != null && !status.isBlank()) {
            try {
                GoodsReceipt.Status st = GoodsReceipt.Status.valueOf(status.trim().toUpperCase());
                spec = spec.and((root, q, cb) -> cb.equal(root.get("status"), st));
            } catch (IllegalArgumentException ignored) { }
        }
        java.time.LocalDateTime __cutoff = org.lvtn.mws.infrastructure.security.scope.CreationDateScopeContext.get();
        if (__cutoff != null) {
            final java.time.LocalDateTime __cf = __cutoff;
            spec = spec.and((root, q, cb) -> cb.greaterThanOrEqualTo(root.<java.time.LocalDateTime>get("receivedAt"), __cf));
        }
        var pageable = org.springframework.data.domain.PageRequest.of(
                pageQuery.page(), pageQuery.size(),
                resolveSort(pageQuery));
        var pageEntity = jpa.findAll(spec, pageable);
        return new org.lvtn.mws.domain.common.PageResult<>(
                pageEntity.getContent().stream().map(mapper::toDomain).toList(),
                pageQuery.page(), pageQuery.size(), pageEntity.getTotalElements());
    }

    @Override
    public boolean existsByGrnNumber(String grnNumber) {
        return jpa.existsByGrnNumber(grnNumber);
    }

    private static org.springframework.data.domain.Sort resolveSort(org.lvtn.mws.domain.common.PageQuery pq) {
        org.springframework.data.domain.Sort def = org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "receivedAt");
        if (pq.sortBy() == null) return def;
        String col = switch (pq.sortBy()) {
            case "grnNumber" -> "grnNumber";
            case "status" -> "status";
            case "receivedAt" -> "receivedAt";
            default -> null;
        };
        if (col == null) return def;
        return org.springframework.data.domain.Sort.by(pq.ascending() ? org.springframework.data.domain.Sort.Direction.ASC : org.springframework.data.domain.Sort.Direction.DESC, col);
    }
}
