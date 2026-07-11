package org.lvtn.mws.infrastructure.persistence.repository.salesorder;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.SalesOrder;
import org.lvtn.mws.domain.model.SalesOrder.Status;
import org.lvtn.mws.domain.repository.ISalesOrderRepository;
import org.lvtn.mws.infrastructure.persistence.mapper.SalesOrderInfraMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SalesOrderRepositoryImpl implements ISalesOrderRepository {

    private final JpaSalesOrderRepository jpa;
    private final SalesOrderInfraMapper mapper;

    @Override
    public SalesOrder save(SalesOrder salesOrder) {
        return mapper.toDomain(jpa.save(mapper.toEntity(salesOrder)));
    }

    @Override
    public Optional<SalesOrder> findById(String id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<SalesOrder> findAll() {
        return jpa.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<SalesOrder> findAllScoped() {
        return jpa.findAll(org.lvtn.mws.infrastructure.security.scope.WarehouseScopeSpecs
                        .<org.lvtn.mws.infrastructure.persistence.entity.SalesOrderEntity>restrict("warehouseId"))
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public org.lvtn.mws.domain.common.PageResult<SalesOrder> search(
            String keyword, String status, org.lvtn.mws.domain.common.PageQuery pageQuery) {
        org.springframework.data.jpa.domain.Specification<org.lvtn.mws.infrastructure.persistence.entity.SalesOrderEntity> spec =
                org.lvtn.mws.infrastructure.security.scope.WarehouseScopeSpecs.restrict("warehouseId");
        if (keyword != null && !keyword.isBlank()) {
            String like = "%" + keyword.trim().toLowerCase() + "%";
            spec = spec.and((root, q, cb) -> cb.like(cb.lower(root.get("soNumber")), like));
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
        var pageable = org.springframework.data.domain.PageRequest.of(
                pageQuery.page(), pageQuery.size(),
                resolveSort(pageQuery));
        var pageEntity = jpa.findAll(spec, pageable);
        return new org.lvtn.mws.domain.common.PageResult<>(
                pageEntity.getContent().stream().map(mapper::toDomain).toList(),
                pageQuery.page(), pageQuery.size(), pageEntity.getTotalElements());
    }

    @Override
    public List<SalesOrder> findByStatus(Status status) {
        return jpa.findByStatus(status.name()).stream().map(mapper::toDomain).toList();
    }

    @Override
    public boolean existsBySoNumber(String soNumber) {
        return jpa.existsBySoNumber(soNumber);
    }

    private static org.springframework.data.domain.Sort resolveSort(org.lvtn.mws.domain.common.PageQuery pq) {
        org.springframework.data.domain.Sort def = org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt");
        if (pq.sortBy() == null) return def;
        String col = switch (pq.sortBy()) {
            case "soNumber" -> "soNumber";
            case "status" -> "status";
            case "priority" -> "priority";
            case "requiredDate" -> "requiredDate";
            case "createdAt" -> "createdAt";
            default -> null;
        };
        if (col == null) return def;
        return org.springframework.data.domain.Sort.by(pq.ascending() ? org.springframework.data.domain.Sort.Direction.ASC : org.springframework.data.domain.Sort.Direction.DESC, col);
    }
}
