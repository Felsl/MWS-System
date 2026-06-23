package org.lvtn.mws.interfaces.dto.request.stocktake;

import jakarta.validation.constraints.NotBlank;

/** Bắt đầu một phiên kiểm kê cho kho. */
public record StartStocktakeRequest(
        @NotBlank(message = "warehouseId không được trống") String warehouseId) {
}
