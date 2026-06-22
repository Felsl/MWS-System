package org.lvtn.mws.infrastructure.persistence.mapper;

import org.lvtn.mws.domain.model.SalesOrder;
import org.lvtn.mws.domain.model.SalesOrderDetail;
import org.lvtn.mws.infrastructure.persistence.entity.SalesOrderDetailEntity;
import org.lvtn.mws.infrastructure.persistence.entity.SalesOrderEntity;
import org.mapstruct.Mapper;

import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring")
public interface SalesOrderInfraMapper {

    default SalesOrderEntity toEntity(SalesOrder domain) {
        if (domain == null) return null;
        SalesOrderEntity e = new SalesOrderEntity();
        e.setId(domain.getId());
        e.setSoNumber(domain.getSoNumber());
        e.setWarehouseId(domain.getWarehouseId());
        e.setCustomerId(domain.getCustomerId());
        e.setDiscountAmount(domain.getDiscountAmount());
        e.setStatus(domain.getStatus().name());
        e.setPriority(domain.getPriority());
        e.setRequiredDate(domain.getRequiredDate());
        e.setCreatedBy(domain.getCreatedBy());
        e.setCreatedAt(domain.getCreatedAt());
        e.setUpdatedAt(domain.getUpdatedAt());

        List<SalesOrderDetailEntity> detailEntities = new ArrayList<>();
        for (SalesOrderDetail d : domain.getDetails()) {
            SalesOrderDetailEntity de = toDetailEntity(d);
            de.setSalesOrder(e); // back-reference cho FK so_id
            detailEntities.add(de);
        }
        e.setDetails(detailEntities);
        return e;
    }

    default SalesOrderDetailEntity toDetailEntity(SalesOrderDetail d) {
        SalesOrderDetailEntity de = new SalesOrderDetailEntity();
        de.setId(d.getId());
        de.setProductId(d.getProductId());
        de.setQuantityOrdered(d.getQuantityOrdered());
        de.setQuantityPicked(d.getQuantityPicked());
        de.setUnitPrice(d.getUnitPrice());
        de.setDiscountPercent(d.getDiscountPercent());
        return de;
    }

    default SalesOrder toDomain(SalesOrderEntity e) {
        if (e == null) return null;
        List<SalesOrderDetail> details = new ArrayList<>();
        if (e.getDetails() != null) {
            for (SalesOrderDetailEntity de : e.getDetails()) {
                details.add(new SalesOrderDetail.Builder()
                        .id(de.getId())
                        .soId(e.getId())
                        .productId(de.getProductId())
                        .quantityOrdered(de.getQuantityOrdered())
                        .quantityPicked(de.getQuantityPicked() == null ? 0 : de.getQuantityPicked())
                        .unitPrice(de.getUnitPrice())
                        .discountPercent(de.getDiscountPercent())
                        .build());
            }
        }
        return new SalesOrder.Builder()
                .id(e.getId())
                .soNumber(e.getSoNumber())
                .warehouseId(e.getWarehouseId())
                .customerId(e.getCustomerId())
                .discountAmount(e.getDiscountAmount())
                .status(SalesOrder.Status.valueOf(e.getStatus()))
                .priority(e.getPriority() == null ? 0 : e.getPriority())
                .requiredDate(e.getRequiredDate())
                .createdBy(e.getCreatedBy())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .details(details)
                .build();
    }
}
