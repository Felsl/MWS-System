package org.lvtn.mws.interfaces.dto.response.picking;

import java.time.LocalDateTime;
import java.util.List;

public record PickingListResponse(
        String id,
        String soId,
        String assignedTo,
        String status,
        LocalDateTime startedAt,
        LocalDateTime completedAt,
        List<PickingListDetailResponse> details) {
}
