package org.lvtn.mws.domain.model;

/**
 * Input DTO (pure domain) carrying per-batch deduction detail for commitStockDeduction.
 */
public class BatchDeductionRequest {

    private final String batchId;
    private final int quantity;

    public BatchDeductionRequest(String batchId, int quantity) {
        this.batchId  = batchId;
        this.quantity = quantity;
    }

    public String getBatchId() { return batchId; }
    public int getQuantity()   { return quantity; }
}
