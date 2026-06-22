package org.lvtn.mws.interfaces.dto.request.shipment;

import jakarta.validation.constraints.NotNull;

/** Gán hãng vận chuyển + mã vận đơn (tracking number) lấy từ API carrier. */
public record AssignTrackingRequest(
        @NotNull(message = "carrierId không được rỗng") String carrierId,
        @NotNull(message = "trackingNumber không được rỗng") String trackingNumber) {
}
