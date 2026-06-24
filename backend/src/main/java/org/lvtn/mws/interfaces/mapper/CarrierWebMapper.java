package org.lvtn.mws.interfaces.mapper;

import org.lvtn.mws.domain.model.Carrier;
import org.lvtn.mws.interfaces.dto.response.carrier.CarrierResponse;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CarrierWebMapper {

    public CarrierResponse toResponse(Carrier c) {
        if (c == null) return null;
        return CarrierResponse.builder()
                .id(c.getId())
                .code(c.getCode())
                .name(c.getName())
                .shippingFeeRule(c.getShippingFeeRule())
                .status(c.getStatus() != null ? c.getStatus().name() : null)
                .build();
    }

    public List<CarrierResponse> toResponseList(List<Carrier> carriers) {
        List<CarrierResponse> list = new ArrayList<>();
        if (carriers != null) {
            for (Carrier c : carriers) list.add(toResponse(c));
        }
        return list;
    }
}
