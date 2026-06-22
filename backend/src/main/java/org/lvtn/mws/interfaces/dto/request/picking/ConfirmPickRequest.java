package org.lvtn.mws.interfaces.dto.request.picking;

import jakarta.validation.constraints.NotNull;

/**
 * Xác nhận quét mã vạch (barcode) cho một dòng gom hàng.
 * scannedBatchNumber = chuỗi BATCH-yyyyMMdd-#### mà công nhân quét được.
 * Số lượng KHÔNG nhập tay: quét đúng lô là nhận trọn quantityToPick mà FEFO engine đã chốt.
 */
public record ConfirmPickRequest(
        @NotNull(message = "scannedBatchNumber không được rỗng") String scannedBatchNumber,
        @NotNull(message = "confirmedBy không được rỗng") String confirmedBy) {
}
