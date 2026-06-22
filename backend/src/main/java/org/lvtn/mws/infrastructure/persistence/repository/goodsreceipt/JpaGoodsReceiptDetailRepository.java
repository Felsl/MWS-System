package org.lvtn.mws.infrastructure.persistence.repository.goodsreceipt;

import org.lvtn.mws.infrastructure.persistence.entity.GoodsReceiptDetailEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaGoodsReceiptDetailRepository extends JpaRepository<GoodsReceiptDetailEntity, String> {
    List<GoodsReceiptDetailEntity> findByGrnId(String grnId);
}
