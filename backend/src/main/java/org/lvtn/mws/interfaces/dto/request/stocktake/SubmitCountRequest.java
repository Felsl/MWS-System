package org.lvtn.mws.interfaces.dto.request.stocktake;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** Nhập số đếm thực tế cho một dòng kiểm kê. */
public record SubmitCountRequest(
        @NotNull(message = "countedQuantity không được trống")
        @Min(value = 0, message = "countedQuantity không được âm")
        Integer countedQuantity) {
}
