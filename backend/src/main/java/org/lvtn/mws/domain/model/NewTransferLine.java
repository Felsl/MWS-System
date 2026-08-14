package org.lvtn.mws.domain.model;

import java.util.Objects;

/** Dòng hàng đầu vào khi lập phiếu điều chuyển (DRAFT). Thuần Java. */
public class NewTransferLine {

    private final String productId;
    private final int quantity;
    /** Lô do người lập/duyệt CHỈ ĐỊNH (null = chỉ gợi ý FEFO, picker quét tự do). */
    private final String designatedBatchId;
    private final String supplierId;
    private final String batchMode;

    public NewTransferLine(String productId, int quantity) {
        this(productId, quantity, null, null, null);
    }

    public NewTransferLine(String productId, int quantity, String designatedBatchId) {
        this(productId, quantity, designatedBatchId, null, null);
    }

    public NewTransferLine(String productId, int quantity, String designatedBatchId,
                           String supplierId, String batchMode) {
        this.productId = Objects.requireNonNull(productId, "productId is required");
        if (quantity <= 0) throw new IllegalArgumentException("Số lượng phải lớn hơn 0");
        this.quantity = quantity;
        this.designatedBatchId = designatedBatchId; // có thể null
        this.supplierId = supplierId;
        this.batchMode = batchMode;
    }

    public String getProductId()         { return productId; }
    public int getQuantity()             { return quantity; }
    public String getDesignatedBatchId() { return designatedBatchId; }
    public String getSupplierId()        { return supplierId; }
    public String getBatchMode()         { return batchMode; }
}
