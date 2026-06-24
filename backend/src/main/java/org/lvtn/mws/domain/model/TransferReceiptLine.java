package org.lvtn.mws.domain.model;

import java.util.Objects;

/** Dòng đối soát nhận hàng tại kho đích. Thuần Java. */
public class TransferReceiptLine {

    private final String detailId;
    private final int quantityReceived;
    private final String binLocationId; // ô kệ cất hàng tại kho đích (thủ kho quét chọn)

    public TransferReceiptLine(String detailId, int quantityReceived, String binLocationId) {
        this.detailId = Objects.requireNonNull(detailId, "detailId is required");
        if (quantityReceived < 0) throw new IllegalArgumentException("Số nhận không được âm");
        this.quantityReceived = quantityReceived;
        this.binLocationId = Objects.requireNonNull(binLocationId, "Ô kệ cất hàng không được trống");
    }

    public String getDetailId()        { return detailId; }
    public int getQuantityReceived()   { return quantityReceived; }
    public String getBinLocationId()   { return binLocationId; }
}
