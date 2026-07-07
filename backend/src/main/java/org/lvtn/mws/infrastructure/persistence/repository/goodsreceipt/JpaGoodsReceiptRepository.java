package org.lvtn.mws.infrastructure.persistence.repository.goodsreceipt;

import org.lvtn.mws.infrastructure.persistence.entity.GoodsReceiptEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface JpaGoodsReceiptRepository
        extends JpaRepository<GoodsReceiptEntity, String>,
                JpaSpecificationExecutor<GoodsReceiptEntity> {
    Optional<GoodsReceiptEntity> findByGrnNumber(String grnNumber);
    boolean existsByGrnNumber(String grnNumber);
    List<GoodsReceiptEntity> findByPoId(String poId);
}
