package org.lvtn.mws.interfaces.mapper;

import org.lvtn.mws.domain.model.BinLocation;
import org.lvtn.mws.domain.model.Warehouse;
import org.lvtn.mws.interfaces.dto.response.warehouse.BinLocationResponse;
import org.lvtn.mws.interfaces.dto.response.warehouse.WarehouseResponse;
import org.springframework.stereotype.Component;

@Component
public class WarehouseWebMapper {

    public WarehouseResponse toResponse(Warehouse domain) {
        if (domain == null) return null;
        return WarehouseResponse.builder()
                .id(domain.getId())
                .code(domain.getCode())
                .name(domain.getName())
                .address(domain.getAddress())
                .status(domain.getStatus())
                .createdAt(domain.getCreatedAt())
                .build();
    }

    public BinLocationResponse toBinResponse(BinLocation domain) {
        if (domain == null) return null;
        return BinLocationResponse.builder()
                .id(domain.getId())
                .warehouseId(domain.getWarehouseId())
                .zone(domain.getZone())
                .aisle(domain.getAisle())
                .rack(domain.getRack())
                .bin(domain.getBin())
                .coordinateLabel(
                    domain.getZone() + "-" + domain.getAisle()
                    + "-" + domain.getRack() + "-" + domain.getBin())
                .build();
    }
}
