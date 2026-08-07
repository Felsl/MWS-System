package org.lvtn.mws.infrastructure.persistence.repository.goodsreceipt;

import org.lvtn.mws.infrastructure.persistence.entity.GoodsReceiptDetailEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JpaGoodsReceiptDetailRepository extends JpaRepository<GoodsReceiptDetailEntity, String> {
    List<GoodsReceiptDetailEntity> findByGrnId(String grnId);

    /**
     * Suy nhà cung cấp của lô: goods_receipt_details -> goods_receipts -> purchase_orders -> suppliers.
     * Trả từng dòng [batchNumber, supplierName, receivedAt] cho (sản phẩm, kho); tầng trên gom map
     * và xử lý trùng batchNumber (giữ lần nhập gần nhất). Theta-join vì các entity dùng khóa String,
     * không có @ManyToOne.
     */
    @Query("SELECT grd.batchNumber, s.name, gr.receivedAt " +
           "FROM GoodsReceiptDetailEntity grd, GoodsReceiptEntity gr, " +
           "     PurchaseOrderEntity po, SupplierEntity s " +
           "WHERE gr.id = grd.grnId AND po.id = gr.poId AND s.id = po.supplierId " +
           "  AND grd.productId = :productId AND gr.warehouseId = :warehouseId " +
           "  AND grd.batchNumber IS NOT NULL")
    List<Object[]> findBatchSupplierRows(@Param("productId") String productId,
                                         @Param("warehouseId") String warehouseId);

    /** [PA1] Suy NCC theo SẢN PHẨM cho một kho: từng dòng [productId, supplierName] (tồn theo kho gộp theo product). */
    @Query("SELECT grd.productId, s.name " +
           "FROM GoodsReceiptDetailEntity grd, GoodsReceiptEntity gr, " +
           "     PurchaseOrderEntity po, SupplierEntity s " +
           "WHERE gr.id = grd.grnId AND po.id = gr.poId AND s.id = po.supplierId " +
           "  AND gr.warehouseId = :warehouseId")
    List<Object[]> findProductSupplierRows(@Param("warehouseId") String warehouseId);
}
