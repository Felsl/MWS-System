package org.lvtn.mws.infrastructure.persistence.repository.purchaseorder;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.PurchaseOrder;
import org.lvtn.mws.domain.repository.IPurchaseOrderRepository;
import org.lvtn.mws.infrastructure.persistence.mapper.PurchaseOrderInfraMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PurchaseOrderRepositoryImpl implements IPurchaseOrderRepository {

    private final JpaPurchaseOrderRepository jpa;
    private final PurchaseOrderInfraMapper mapper;

    @Override
    public PurchaseOrder save(PurchaseOrder purchaseOrder) {
        return mapper.toDomain(jpa.save(mapper.toEntity(purchaseOrder)));
    }

    @Override
    public Optional<PurchaseOrder> findById(String id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<PurchaseOrder> findByPoNumber(String poNumber) {
        return jpa.findByPoNumber(poNumber).map(mapper::toDomain);
    }

    @Override
    public List<PurchaseOrder> findAll() {
        return mapper.toDomainList(jpa.findAll());
    }

    @Override
    public org.lvtn.mws.domain.common.PageResult<PurchaseOrder> search(
            String keyword, String status, org.lvtn.mws.domain.common.PageQuery pageQuery) {
        org.springframework.data.jpa.domain.Specification<org.lvtn.mws.infrastructure.persistence.entity.PurchaseOrderEntity> spec =
                org.lvtn.mws.infrastructure.security.scope.WarehouseScopeSpecs.restrict("warehouseId");
        if (keyword != null && !keyword.isBlank()) {
            String like = "%" + keyword.trim().toLowerCase() + "%";
            spec = spec.and((root, q, cb) -> cb.like(cb.lower(root.get("poNumber")), like));
        }
        if (status != null && !status.isBlank()) {
            try {
                PurchaseOrder.Status st = PurchaseOrder.Status.valueOf(status.trim().toUpperCase());
                spec = spec.and((root, q, cb) -> cb.equal(root.get("status"), st));
            } catch (IllegalArgumentException ignored) { }
        }
        var pageable = org.springframework.data.domain.PageRequest.of(
                pageQuery.page(), pageQuery.size(),
                org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt"));
        var pageEntity = jpa.findAll(spec, pageable);
        return new org.lvtn.mws.domain.common.PageResult<>(
                pageEntity.getContent().stream().map(mapper::toDomain).toList(),
                pageQuery.page(), pageQuery.size(), pageEntity.getTotalElements());
    }

    @Override
    public boolean existsByPoNumber(String poNumber) {
        return jpa.existsByPoNumber(poNumber);
    }
}
