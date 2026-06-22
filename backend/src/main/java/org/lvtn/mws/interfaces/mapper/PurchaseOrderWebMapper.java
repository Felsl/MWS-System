package org.lvtn.mws.interfaces.mapper;

import org.lvtn.mws.domain.model.PurchaseOrder;
import org.lvtn.mws.domain.model.PurchaseOrderDetail;
import org.lvtn.mws.domain.model.PurchaseOrderLineCommand;
import org.lvtn.mws.interfaces.dto.request.purchaseorder.PurchaseOrderLineRequest;
import org.lvtn.mws.interfaces.dto.response.purchaseorder.PurchaseOrderDetailResponse;
import org.lvtn.mws.interfaces.dto.response.purchaseorder.PurchaseOrderResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Web layer mapper for the purchase-order endpoints.
 * MapStruct auto-generates the domain -> response mapping (the {@code status} enum is
 * mapped to its {@code name()} automatically). The request -> command conversion is
 * hand-written because the command is an immutable value object.
 */
@Mapper(componentModel = "spring")
public interface PurchaseOrderWebMapper {

    PurchaseOrderDetailResponse toDetailResponse(PurchaseOrderDetail detail);

    List<PurchaseOrderDetailResponse> toDetailResponses(List<PurchaseOrderDetail> details);

    @Mapping(target = "details", ignore = true)
    PurchaseOrderResponse toResponse(PurchaseOrder po);

    /** Combines the PO header with its detail lines into a single response. */
    default PurchaseOrderResponse toResponse(PurchaseOrder po, List<PurchaseOrderDetail> details) {
        PurchaseOrderResponse response = toResponse(po);
        response.setDetails(toDetailResponses(details));
        return response;
    }

    /** Request lines -> domain command value objects. */
    default List<PurchaseOrderLineCommand> toCommands(List<PurchaseOrderLineRequest> lines) {
        if (lines == null) {
            return Collections.emptyList();
        }
        return lines.stream()
                .map(line -> new PurchaseOrderLineCommand(
                        line.getProductId(),
                        line.getQuantityOrdered(),
                        line.getUnitPrice()))
                .collect(Collectors.toList());
    }
}
