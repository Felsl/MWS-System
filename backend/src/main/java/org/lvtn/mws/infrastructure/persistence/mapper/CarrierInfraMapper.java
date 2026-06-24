package org.lvtn.mws.infrastructure.persistence.mapper;

import org.lvtn.mws.domain.model.Carrier;
import org.lvtn.mws.infrastructure.persistence.entity.CarrierEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CarrierInfraMapper {

    default CarrierEntity toEntity(Carrier c) {
        if (c == null) return null;
        CarrierEntity e = new CarrierEntity();
        e.setId(c.getId());
        e.setCode(c.getCode());
        e.setName(c.getName());
        e.setShippingFeeRule(c.getShippingFeeRule());
        e.setStatus(c.getStatus());
        return e;
    }

    default Carrier toDomain(CarrierEntity e) {
        if (e == null) return null;
        return new Carrier.Builder()
                .id(e.getId())
                .code(e.getCode())
                .name(e.getName())
                .shippingFeeRule(e.getShippingFeeRule())
                .status(e.getStatus())
                .build();
    }
}
