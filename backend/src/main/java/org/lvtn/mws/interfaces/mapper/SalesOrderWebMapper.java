package org.lvtn.mws.interfaces.mapper;

import org.lvtn.mws.domain.model.SalesOrder;
import org.lvtn.mws.domain.model.SalesOrderDetail;
import org.lvtn.mws.interfaces.dto.response.salesorder.SalesOrderDetailResponse;
import org.lvtn.mws.interfaces.dto.response.salesorder.SalesOrderResponse;
import org.mapstruct.Mapper;

import java.util.List;

/** Domain -> Response (một chiều). */
@Mapper(componentModel = "spring")
public interface SalesOrderWebMapper {

    default SalesOrderResponse toResponse(SalesOrder so) {
        if (so == null) return null;
        return new SalesOrderResponse(
                so.getId(),
                so.getSoNumber(),
                so.getWarehouseId(),
                so.getCustomerId(),
                so.getDiscountAmount(),
                so.getStatus() == null ? null : so.getStatus().name(),
                so.getPriority(),
                so.getRequiredDate(),
                so.getCreatedBy(),
                so.getCreatedAt(),
                so.getUpdatedAt(),
                toDetailResponseList(so.getDetails()));
    }

    default SalesOrderDetailResponse toDetailResponse(SalesOrderDetail d) {
        if (d == null) return null;
        return new SalesOrderDetailResponse(
                d.getId(), d.getSoId(), d.getProductId(),
                d.getQuantityOrdered(), d.getQuantityPicked(),
                d.getUnitPrice(), d.getDiscountPercent());
    }

    default List<SalesOrderDetailResponse> toDetailResponseList(List<SalesOrderDetail> list) {
        if (list == null) return List.of();
        return list.stream().map(this::toDetailResponse).toList();
    }

    default List<SalesOrderResponse> toResponseList(List<SalesOrder> list) {
        if (list == null) return List.of();
        return list.stream().map(this::toResponse).toList();
    }
}
