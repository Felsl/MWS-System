package org.lvtn.mws.domain.model;

import java.math.BigDecimal;

/** Pure value object carrying a PO line during creation. */
public class PurchaseOrderLineCommand {
    private final String productId;
    private final int quantityOrdered;
    private final BigDecimal unitPrice;
    private final String supplierId;

    public PurchaseOrderLineCommand(String productId, int quantityOrdered, BigDecimal unitPrice, String supplierId) {
        this.productId = productId;
        this.quantityOrdered = quantityOrdered;
        this.unitPrice = unitPrice;
        this.supplierId = supplierId;
    }
    public String getProductId()    { return productId; }
    public int getQuantityOrdered() { return quantityOrdered; }
    public BigDecimal getUnitPrice(){ return unitPrice; }
    public String getSupplierId()   { return supplierId; }
}
