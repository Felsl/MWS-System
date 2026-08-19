package org.lvtn.mws.infrastructure.persistence.repository.goodsreceipt;

import org.lvtn.mws.infrastructure.persistence.entity.GoodsReceiptDetailEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JpaGoodsReceiptDetailRepository extends JpaRepository<GoodsReceiptDetailEntity, String> {
    List<GoodsReceiptDetailEntity> findByGrnId(String grnId);

    /**
     * Suy nhà cung cấp của lô. Hai nguồn NCC có thể có:
     *   1) grd.supplierId  — chọn trực tiếp theo dòng khi nhập tự do (không PO). Ưu tiên.
     *   2) po.supplierId   — suy qua PO khi nhập từ đơn mua.
     * Trước đây chỉ dùng đường (2) qua INNER JOIN nên phiếu nhập tự do không có NCC → BE trả null → FE hiện "—".
     * Nay dùng LEFT JOIN cả hai và COALESCE để cover đủ.
     * Trả từng dòng [batchNumber, supplierName, receivedAt] cho (sản phẩm, kho); tầng trên gom map
     * và xử lý trùng batchNumber (giữ lần nhập gần nhất). Vẫn theta-join vì các entity dùng khóa String,
     * không có @ManyToOne.
     */
    @Query("SELECT grd.batchNumber, " +
           "  COALESCE(" +
           "    (SELECT sDirect.name FROM SupplierEntity sDirect WHERE sDirect.id = grd.supplierId)," +
           "    (SELECT sViaPo.name FROM SupplierEntity sViaPo, PurchaseOrderEntity po " +
           "      WHERE po.id = gr.poId AND sViaPo.id = po.supplierId)" +
           "  ), " +
           "  gr.receivedAt " +
           "FROM GoodsReceiptDetailEntity grd, GoodsReceiptEntity gr " +
           "WHERE gr.id = grd.grnId " +
           "  AND grd.productId = :productId AND gr.warehouseId = :warehouseId " +
           "  AND grd.batchNumber IS NOT NULL")
    List<Object[]> findBatchSupplierRows(@Param("productId") String productId,
                                         @Param("warehouseId") String warehouseId);

    /** [PA1] Suy NCC theo SẢN PHẨM cho một kho: từng dòng [productId, supplierName].
     *  Cùng cách xử lý 2 nguồn NCC như trên. */
    @Query("SELECT grd.productId, " +
           "  COALESCE(" +
           "    (SELECT sDirect.name FROM SupplierEntity sDirect WHERE sDirect.id = grd.supplierId)," +
           "    (SELECT sViaPo.name FROM SupplierEntity sViaPo, PurchaseOrderEntity po " +
           "      WHERE po.id = gr.poId AND sViaPo.id = po.supplierId)" +
           "  ) " +
           "FROM GoodsReceiptDetailEntity grd, GoodsReceiptEntity gr " +
           "WHERE gr.id = grd.grnId AND gr.warehouseId = :warehouseId")
    List<Object[]> findProductSupplierRows(@Param("warehouseId") String warehouseId);
}
