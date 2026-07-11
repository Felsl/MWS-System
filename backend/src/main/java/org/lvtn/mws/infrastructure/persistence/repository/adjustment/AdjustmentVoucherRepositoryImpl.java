package org.lvtn.mws.infrastructure.persistence.repository.adjustment;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.AdjustmentVoucher;
import org.lvtn.mws.domain.repository.IAdjustmentVoucherRepository;
import org.lvtn.mws.infrastructure.persistence.mapper.AdjustmentVoucherInfraMapper;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.*;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AdjustmentVoucherRepositoryImpl implements IAdjustmentVoucherRepository {

    private final JpaAdjustmentVoucherRepository jpa;
    private final AdjustmentVoucherInfraMapper mapper;

    @Override
    public AdjustmentVoucher save(AdjustmentVoucher voucher) {
        return mapper.toDomain(jpa.save(mapper.toEntity(voucher)));
    }

    @Override
    public Optional<AdjustmentVoucher> findById(String id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<AdjustmentVoucher> findAll() {
        return jpa.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public org.lvtn.mws.domain.common.PageResult<AdjustmentVoucher> search(
            String keyword, String status, org.lvtn.mws.domain.common.PageQuery pageQuery) {
        Specification<org.lvtn.mws.infrastructure.persistence.entity.AdjustmentVoucherEntity> spec =
                org.lvtn.mws.infrastructure.security.scope.WarehouseScopeSpecs.restrict("warehouseId");
        if (keyword != null && !keyword.isBlank()) {
            String like = "%" + keyword.trim().toLowerCase() + "%";
            spec = spec.and((root, q, cb) -> cb.like(cb.lower(root.get("voucherNumber")), like));
        }
        if (status != null && !status.isBlank()) {
            String st = status.trim();
            spec = spec.and((root, q, cb) -> cb.equal(root.get("status"), st));
        }
        java.time.LocalDateTime __cutoff = org.lvtn.mws.infrastructure.security.scope.CreationDateScopeContext.get();
        if (__cutoff != null) {
            final java.time.LocalDateTime __cf = __cutoff;
            spec = spec.and((root, q, cb) -> cb.greaterThanOrEqualTo(root.<java.time.LocalDateTime>get("createdAt"), __cf));
        }
        var pageable = PageRequest.of(
                pageQuery.page(), pageQuery.size(),
                resolveSort(pageQuery));
        var pageEntity = jpa.findAll(spec, pageable);
        return new org.lvtn.mws.domain.common.PageResult<>(
                pageEntity.getContent().stream().map(mapper::toDomain).toList(),
                pageQuery.page(), pageQuery.size(), pageEntity.getTotalElements());
    }

    @Override
    public List<AdjustmentVoucher> findAllScoped() {
        return jpa.findAll(org.lvtn.mws.infrastructure.security.scope.WarehouseScopeSpecs
                        .<org.lvtn.mws.infrastructure.persistence.entity.AdjustmentVoucherEntity>restrict("warehouseId"))
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<AdjustmentVoucher> findBySessionId(String sessionId) {
        return jpa.findBySessionId(sessionId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public boolean existsByVoucherNumber(String voucherNumber) {
        return jpa.existsByVoucherNumber(voucherNumber);
    }

    private static org.springframework.data.domain.Sort resolveSort(org.lvtn.mws.domain.common.PageQuery pq) {
        org.springframework.data.domain.Sort def = org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt");
        if (pq.sortBy() == null) return def;
        String col = switch (pq.sortBy()) {
            case "voucherNumber" -> "voucherNumber";
            case "status" -> "status";
            case "createdAt" -> "createdAt";
            default -> null;
        };
        if (col == null) return def;
        return org.springframework.data.domain.Sort.by(pq.ascending() ? org.springframework.data.domain.Sort.Direction.ASC : org.springframework.data.domain.Sort.Direction.DESC, col);
    }
}
