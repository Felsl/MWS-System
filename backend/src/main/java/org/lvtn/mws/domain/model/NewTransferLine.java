package org.lvtn.mws.domain.model;

import java.util.Objects;

/** Dòng hàng đầu vào khi lập phiếu điều chuyển (DRAFT). Thuần Java. */
public class NewTransferLine {

    private final String productId;
    private final int quantity;
    /** Lô do người lập/duyệt CHỈ ĐỊNH (null = chỉ gợi ý FEFO, picker quét tự do). */
    private final String designatedBatchId;

    public NewTransferLine(String productId, int quantity) {
        this(productId, quantity, null);
    }

    public NewTransferLine(String productId, int quantity, String designatedBatchId) {
        this.productId = Objects.requireNonNull(productId, "productId is required");
        if (quantity <= 0) throw new IllegalArgumentException("Số lượng phải lớn hơn 0");
        this.quantity = quantity;
        this.designatedBatchId = designatedBatchId; // có thể null
    }

    public String getProductId()         { return productId; }
    public int getQuantity()             { return quantity; }
    public String getDesignatedBatchId() { return designatedBatchId; }
}
