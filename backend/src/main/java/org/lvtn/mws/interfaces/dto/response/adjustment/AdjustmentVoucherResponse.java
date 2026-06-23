package org.lvtn.mws.interfaces.dto.response.adjustment;

import java.time.LocalDateTime;
import java.util.List;

public record AdjustmentVoucherResponse(
        String id,
        String voucherNumber,
        String warehouseId,
        String sessionId,
        String reason,
        String status,
        String createdBy,
        String approvedBy,
        LocalDateTime createdAt,
        double maxVariancePercent,
        List<AdjustmentVoucherDetailResponse> details) {
}
