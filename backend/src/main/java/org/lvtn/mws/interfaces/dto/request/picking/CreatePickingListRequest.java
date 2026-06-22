package org.lvtn.mws.interfaces.dto.request.picking;

import jakarta.validation.constraints.NotNull;

/** Tạo lệnh gom hàng (FEFO) cho một đơn bán hàng đã ALLOCATED. */
public record CreatePickingListRequest(
        @NotNull(message = "soId không được rỗng") String soId) {
}
