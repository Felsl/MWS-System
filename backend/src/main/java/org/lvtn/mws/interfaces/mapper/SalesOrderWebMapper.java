package org.lvtn.mws.interfaces.mapper;

import org.lvtn.mws.domain.model.SalesOrder;
import org.lvtn.mws.domain.model.SalesOrderDetail;
import org.lvtn.mws.domain.model.User;
import org.lvtn.mws.domain.repository.IUserRepository;
import org.lvtn.mws.interfaces.dto.response.salesorder.SalesOrderDetailResponse;
import org.lvtn.mws.interfaces.dto.response.salesorder.SalesOrderResponse;
import org.springframework.stereotype.Component;

import java.util.List;

/** Domain -> Response (một chiều). Resolve người tạo (userId -> username). */
@Component
public class SalesOrderWebMapper {

    private final IUserRepository userRepository;

    public SalesOrderWebMapper(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public SalesOrderResponse toResponse(SalesOrder so) {
        if (so == null) return null;
        String createdByName = so.getCreatedBy() == null ? null
                : userRepository.findById(so.getCreatedBy()).map(User::getUsername).orElse(so.getCreatedBy());
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
                createdByName,
                so.getCreatedAt(),
                so.getUpdatedAt(),
                toDetailResponseList(so.getDetails()));
    }

    public SalesOrderDetailResponse toDetailResponse(SalesOrderDetail d) {
        if (d == null) return null;
        return new SalesOrderDetailResponse(
                d.getId(), d.getSoId(), d.getProductId(), d.getSupplierId(),
                d.getQuantityOrdered(), d.getQuantityPicked(), d.getQuantityAllocated(),
                d.getUnitPrice(), d.getDiscountPercent());
    }

    public List<SalesOrderDetailResponse> toDetailResponseList(List<SalesOrderDetail> list) {
        if (list == null) return List.of();
        return list.stream().map(this::toDetailResponse).toList();
    }

    public List<SalesOrderResponse> toResponseList(List<SalesOrder> list) {
        if (list == null) return List.of();
        return list.stream().map(this::toResponse).toList();
    }
}
