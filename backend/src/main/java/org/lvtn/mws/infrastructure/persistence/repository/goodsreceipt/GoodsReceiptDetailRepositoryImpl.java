package org.lvtn.mws.infrastructure.persistence.repository.goodsreceipt;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.GoodsReceiptDetail;
import org.lvtn.mws.domain.repository.IGoodsReceiptDetailRepository;
import org.lvtn.mws.infrastructure.persistence.mapper.GoodsReceiptDetailInfraMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class GoodsReceiptDetailRepositoryImpl implements IGoodsReceiptDetailRepository {

    private final JpaGoodsReceiptDetailRepository jpa;
    private final GoodsReceiptDetailInfraMapper mapper;

    @Override
    public GoodsReceiptDetail save(GoodsReceiptDetail detail) {
        return mapper.toDomain(jpa.save(mapper.toEntity(detail)));
    }

    @Override
    public void saveAll(List<GoodsReceiptDetail> details) {
        jpa.saveAll(mapper.toEntityList(details));
    }

    @Override
    public List<GoodsReceiptDetail> findByGrnId(String grnId) {
        return mapper.toDomainList(jpa.findByGrnId(grnId));
    }
}
