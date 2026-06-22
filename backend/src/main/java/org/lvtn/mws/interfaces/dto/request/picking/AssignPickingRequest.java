package org.lvtn.mws.interfaces.dto.request.picking;

import jakarta.validation.constraints.NotNull;

/** Giao lệnh gom hàng cho một nhân viên kho. */
public record AssignPickingRequest(
        @NotNull(message = "userId không được rỗng") String userId) {
}
