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
    public boolean existsByGrnNumber(String grnNumber) {
        return jpa.existsByGrnNumber(grnNumber);
    }
}
