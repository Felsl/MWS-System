package org.lvtn.mws.infrastructure.persistence.repository.transfer;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.TransferOrder;
import org.lvtn.mws.domain.model.TransferOrderDetail;
import org.lvtn.mws.domain.repository.ITransferOrderRepository;
import org.lvtn.mws.infrastructure.persistence.entity.TransferOrderDetailEntity;
import org.lvtn.mws.infrastructure.persistence.entity.TransferOrderEntity;
import org.lvtn.mws.infrastructure.persistence.mapper.TransferOrderDetailInfraMapper;
import org.lvtn.mws.infrastructure.persistence.mapper.TransferOrderInfraMapper;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.domain.*;
import org.springframework.data.domain.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Adapter persistence cho phiếu điều chuyển.
 * save(): lưu header + đồng bộ details theo kiểu delete-then-insert (đơn giản, đúng
 * cho aggregate nhỏ; phiếu chỉ có vài dòng). findById(): lắp ráp header + details.
 */
@Repository
@RequiredArgsConstructor
public class TransferOrderRepositoryImpl implements ITransferOrderRepository {

    private final JpaTransferOrderRepository jpaTransferOrderRepository;
    private final JpaTransferOrderDetailRepository jpaDetailRepository;
    private final TransferOrderInfraMapper orderMapper;
    private final TransferOrderDetailInfraMapper detailMapper;

    @Override
    public TransferOrder save(TransferOrder transferOrder) {
        // 1) Lưu header
        TransferOrderEntity headerEntity = orderMapper.toEntity(transferOrder);
        jpaTransferOrderRepository.save(headerEntity);

        // 2) Đồng bộ details: xoá cũ rồi ghi lại (aggregate nhỏ nên chấp nhận được)
        jpaDetailRepository.deleteByTransferOrderId(transferOrder.getId());
        List<TransferOrderDetail> details = transferOrder.getDetails();
        if (details != null && !details.isEmpty()) {
            List<TransferOrderDetailEntity> detailEntities = new ArrayList<>();
            for (TransferOrderDetail d : details) {
                detailEntities.add(detailMapper.toEntity(d));
            }
            jpaDetailRepository.saveAll(detailEntities);
        }

        return transferOrder;
    }

    @Override
    public Optional<TransferOrder> findById(String id) {
        return jpaTransferOrderRepository.findById(id)
                .map(this::assemble);
    }

    @Override
    public List<TransferOrder> findAll() {
        List<TransferOrder> result = new ArrayList<>();
        for (TransferOrderEntity e : jpaTransferOrderRepository.findAll()) {
            result.add(assemble(e));
        }
        return result;
    }

    @Override
    public List<TransferOrder> findAllScoped() {
        Specification<TransferOrderEntity> spec =
                org.lvtn.mws.infrastructure.security.scope.WarehouseScopeSpecs
                        .restrictAny("fromWarehouseId", "toWarehouseId");
        List<TransferOrder> result = new ArrayList<>();
        for (TransferOrderEntity e : jpaTransferOrderRepository.findAll(spec)) {
            result.add(assemble(e));
        }
        return result;
    }

    @Override
    public org.lvtn.mws.domain.common.PageResult<TransferOrder> search(
            String keyword, String status, org.lvtn.mws.domain.common.PageQuery pageQuery) {
        Specification<TransferOrderEntity> spec =
                org.lvtn.mws.infrastructure.security.scope.WarehouseScopeSpecs
                        .restrictAny("fromWarehouseId", "toWarehouseId");
        if (keyword != null && !keyword.isBlank()) {
            String like = "%" + keyword.trim().toLowerCase() + "%";
            spec = spec.and((root, q, cb) -> cb.like(cb.lower(root.get("transferNumber")), like));
        }
        if (status != null && !status.isBlank()) {
            try {
                TransferOrder.Status st = TransferOrder.Status.valueOf(status.trim().toUpperCase());
                spec = spec.and((root, q, cb) -> cb.equal(root.get("status"), st));
            } catch (IllegalArgumentException ignored) { }
        }
        java.time.LocalDateTime __cutoff = org.lvtn.mws.infrastructure.security.scope.CreationDateScopeContext.get();
        if (__cutoff != null) {
            final java.time.LocalDateTime __cf = __cutoff;
            spec = spec.and((root, q, cb) -> cb.greaterThanOrEqualTo(root.<java.time.LocalDateTime>get("createdAt"), __cf));
        }
        var pageable = PageRequest.of(
                pageQuery.page(), pageQuery.size(),
                resolveSort(pageQuery));
        var pageEntity = jpaTransferOrderRepository.findAll(spec, pageable);
        List<TransferOrder> content = new ArrayList<>();
        for (TransferOrderEntity e : pageEntity.getContent()) content.add(assemble(e));
        return new org.lvtn.mws.domain.common.PageResult<>(
                content, pageQuery.page(), pageQuery.size(), pageEntity.getTotalElements());
    }

    @Override
    public List<TransferOrder> findByStatus(TransferOrder.Status status) {
        List<TransferOrder> result = new ArrayList<>();
        for (TransferOrderEntity e : jpaTransferOrderRepository.findByStatus(status)) {
            result.add(assemble(e));
        }
        return result;
    }

    @Override
    public boolean existsByTransferNumber(String transferNumber) {
        return jpaTransferOrderRepository.existsByTransferNumber(transferNumber);
    }

    // ── helper: header + details -> domain aggregate ────────────────────────────
    private TransferOrder assemble(TransferOrderEntity headerEntity) {
        List<TransferOrderDetail> details = new ArrayList<>();
        for (TransferOrderDetailEntity de : jpaDetailRepository.findByTransferOrderId(headerEntity.getId())) {
            details.add(detailMapper.toDomain(de));
        }
        return orderMapper.toDomain(headerEntity, details);
    }

    private static org.springframework.data.domain.Sort resolveSort(org.lvtn.mws.domain.common.PageQuery pq) {
        org.springframework.data.domain.Sort def = org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt");
        if (pq.sortBy() == null) return def;
        String col = switch (pq.sortBy()) {
            case "transferNumber" -> "transferNumber";
            case "status" -> "status";
            case "approvedAt" -> "approvedAt";
            case "createdAt" -> "createdAt";
            default -> null;
        };
        if (col == null) return def;
        return org.springframework.data.domain.Sort.by(pq.ascending() ? org.springframework.data.domain.Sort.Direction.ASC : org.springframework.data.domain.Sort.Direction.DESC, col);
    }
}
