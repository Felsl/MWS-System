package org.lvtn.mws.infrastructure.persistence.repository.purchaseorder;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.PurchaseOrderDetail;
import org.lvtn.mws.domain.repository.IPurchaseOrderDetailRepository;
import org.lvtn.mws.infrastructure.persistence.mapper.PurchaseOrderDetailInfraMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PurchaseOrderDetailRepositoryImpl implements IPurchaseOrderDetailRepository {

    private final JpaPurchaseOrderDetailRepository jpa;
    private final PurchaseOrderDetailInfraMapper mapper;

    @Override
    public PurchaseOrderDetail save(PurchaseOrderDetail detail) {
        return mapper.toDomain(jpa.save(mapper.toEntity(detail)));
    }

    @Override
    public void saveAll(List<PurchaseOrderDetail> details) {
        jpa.saveAll(mapper.toEntityList(details));
    }

    @Override
    public Optional<PurchaseOrderDetail> findById(String id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<PurchaseOrderDetail> findByPoId(String poId) {
        return mapper.toDomainList(jpa.findByPoId(poId));
    }
}
