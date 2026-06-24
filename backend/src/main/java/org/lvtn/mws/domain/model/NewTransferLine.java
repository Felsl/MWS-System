package org.lvtn.mws.domain.model;

import java.util.Objects;

/** Dòng hàng đầu vào khi lập phiếu điều chuyển (DRAFT). Thuần Java. */
public class NewTransferLine {

    private final String productId;
    private final int quantity;

    public NewTransferLine(String productId, int quantity) {
        this.productId = Objects.requireNonNull(productId, "productId is required");
        if (quantity <= 0) throw new IllegalArgumentException("Số lượng phải lớn hơn 0");
        this.quantity = quantity;
    }

    public String getProductId() { return productId; }
    public int getQuantity()     { return quantity; }
}
