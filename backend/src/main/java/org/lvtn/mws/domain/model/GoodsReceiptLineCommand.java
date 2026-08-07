package org.lvtn.mws.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Pure value object carrying a GRN line during creation. */
public class GoodsReceiptLineCommand {
    private final String productId;
    private final String poDetailId;   // nullable
    private final int quantity;
    private final String batchNumber;  // nullable
    private final LocalDate expiryDate; // nullable
    private final String binLocationId;
    private final String supplierId;
    private final BigDecimal unitPrice;

    public GoodsReceiptLineCommand(String productId, String poDetailId, int quantity,
                                   String batchNumber, LocalDate expiryDate, String binLocationId,
                                   String supplierId, BigDecimal unitPrice) {
        this.productId = productId;
        this.poDetailId = poDetailId;
        this.quantity = quantity;
        this.batchNumber = batchNumber;
        this.expiryDate = expiryDate;
        this.binLocationId = binLocationId;
        this.supplierId = supplierId;
        this.unitPrice = unitPrice;
    }
    public String getProductId()    { return productId; }
    public String getPoDetailId()   { return poDetailId; }
    public int getQuantity()        { return quantity; }
    public String getBatchNumber()  { return batchNumber; }
    public LocalDate getExpiryDate(){ return expiryDate; }
    public String getBinLocationId(){ return binLocationId; }
    public String getSupplierId()   { return supplierId; }
    public BigDecimal getUnitPrice(){ return unitPrice; }
}
