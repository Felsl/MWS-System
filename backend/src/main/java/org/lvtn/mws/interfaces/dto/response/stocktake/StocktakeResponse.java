package org.lvtn.mws.interfaces.dto.response.stocktake;

import java.util.List;

/** Phản hồi đầy đủ một phiên kiểm kê: header + danh sách dòng đối chiếu. */
public record StocktakeResponse(
        StocktakeSessionResponse session,
        List<StocktakeDetailResponse> details) {
}
