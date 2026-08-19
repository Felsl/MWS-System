package org.lvtn.mws.interfaces.dto.response.picking;

import java.time.LocalDateTime;
import java.util.List;

public record PickingListResponse(
        String id,
        String pickNumber,
        String soId,
        String soNumber,
        String transferOrderId,
        String transferNumber,
        String assignedTo,
        String assignedToName,
        String status,
        LocalDateTime startedAt,
        LocalDateTime completedAt,
        List<PickingListDetailResponse> details) {
}
