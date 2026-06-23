package org.lvtn.mws.interfaces.dto.response.stocktake;

import org.lvtn.mws.interfaces.dto.response.adjustment.AdjustmentVoucherResponse;

import java.util.List;

/**
 * Kết quả hoàn tất phiên kiểm kê: phiên đã ADJUSTED + phiếu điều chỉnh DRAFT sinh tự động
 * (voucher = null khi không có chênh lệch).
 */
public record CompleteStocktakeResponse(
        StocktakeSessionResponse session,
        List<StocktakeDetailResponse> details,
        AdjustmentVoucherResponse voucher) {
}
