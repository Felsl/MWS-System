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
    public boolean existsByPoNumber(String poNumber) {
        return jpa.existsByPoNumber(poNumber);
    }
}
