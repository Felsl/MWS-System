package org.lvtn.mws.domain.model;

/**
 * FEFO allocation result: tells the picking list exactly where to fetch goods from.
 */
public class BatchSuggestion {

    private final String batchId;
    private final String batchNumber;
    private final String binLocationId;
    private final int suggestedQuantity;

    public BatchSuggestion(String batchId, String batchNumber,
                           String binLocationId, int suggestedQuantity) {
        this.batchId           = batchId;
        this.batchNumber       = batchNumber;
        this.binLocationId     = binLocationId;
        this.suggestedQuantity = suggestedQuantity;
    }

    public String getBatchId()          { return batchId; }
    public String getBatchNumber()      { return batchNumber; }
    public String getBinLocationId()    { return binLocationId; }
    public int getSuggestedQuantity()   { return suggestedQuantity; }
}
