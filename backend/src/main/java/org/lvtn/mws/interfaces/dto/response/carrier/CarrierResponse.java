package org.lvtn.mws.interfaces.dto.response.carrier;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CarrierResponse {
    private String id;
    private String code;
    private String name;
    private String shippingFeeRule;
    private String status;
}
