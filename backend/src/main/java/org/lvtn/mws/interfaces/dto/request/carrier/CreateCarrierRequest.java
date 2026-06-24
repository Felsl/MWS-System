package org.lvtn.mws.interfaces.dto.request.carrier;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateCarrierRequest {

    @NotBlank(message = "code không được trống")
    private String code;

    @NotBlank(message = "name không được trống")
    private String name;

    /** Chuỗi JSON cấu hình phí ship, ví dụ {"baseFee":10000,"perUnitFee":500}. Có thể null. */
    private String shippingFeeRule;
}
