package org.lvtn.mws.interfaces.dto.response.inventory;

/**
 * [Bán theo NCC] Sản phẩm CÓ THỂ BÁN ở một kho (tồn ACTIVE + chưa hết hạn, đã trừ giữ chỗ).
 * Dùng cho dropdown chọn sản phẩm ở màn tạo đơn bán.
 */
public record SellableProductResponse(
        String productId,
        int availableQuantity) {
}
