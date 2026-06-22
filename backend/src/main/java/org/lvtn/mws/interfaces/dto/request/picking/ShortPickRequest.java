package org.lvtn.mws.interfaces.dto.request.picking;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Báo thiếu hàng thực tế (short-pick) cho một dòng nhặt.
 * actualQty = số thực lấy được (phải nhỏ hơn quantityToPick của dòng);
 * phần thiếu sẽ được hệ thống tự bù từ lô FEFO kế tiếp.
 */
public record ShortPickRequest(
        @NotNull(message = "scannedBatchNumber không được rỗng") String scannedBatchNumber,
        @Min(value = 0, message = "Số thực lấy không được âm") int actualQty,
        @NotNull(message = "reason không được rỗng (lý do thiếu hàng)") String reason,
        @NotNull(message = "confirmedBy không được rỗng") String confirmedBy) {
}
