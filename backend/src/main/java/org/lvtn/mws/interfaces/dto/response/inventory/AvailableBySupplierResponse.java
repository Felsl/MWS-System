package org.lvtn.mws.interfaces.dto.response.inventory;

/**
 * [Bán theo NCC] Tồn KHẢ DỤNG (đã trừ giữ chỗ) gom theo nhà cung cấp cho một (sản phẩm, kho).
 * supplierId/supplierName có thể null cho lô chưa gắn NCC (nhập trước khi có tính năng).
 */
public record AvailableBySupplierResponse(
        String supplierId,
        String supplierName,
        int availableQuantity) {
}
