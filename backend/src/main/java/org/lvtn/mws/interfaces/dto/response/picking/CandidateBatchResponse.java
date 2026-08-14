package org.lvtn.mws.interfaces.dto.response.picking;

/** [Báo thiếu] Một lô ứng viên người lấy có thể chọn khi báo thiếu (cùng SP + NCC + kho). */
public record CandidateBatchResponse(
        String batchId,
        String batchNumber,
        String supplierId,
        String supplierName,
        String binLocationId,
        int quantity) {
}
