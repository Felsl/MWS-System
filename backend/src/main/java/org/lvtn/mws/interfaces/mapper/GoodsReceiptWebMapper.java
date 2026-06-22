package org.lvtn.mws.interfaces.mapper;

import org.lvtn.mws.domain.model.GoodsReceipt;
import org.lvtn.mws.domain.model.GoodsReceiptDetail;
import org.lvtn.mws.domain.model.GoodsReceiptLineCommand;
import org.lvtn.mws.interfaces.dto.request.goodsreceipt.GoodsReceiptLineRequest;
import org.lvtn.mws.interfaces.dto.response.goodsreceipt.GoodsReceiptDetailResponse;
import org.lvtn.mws.interfaces.dto.response.goodsreceipt.GoodsReceiptResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Web layer mapper for the goods-receipt endpoints.
 * MapStruct auto-generates the domain -> response mapping; the request -> command
 * conversion is hand-written because the command is an immutable value object.
 */
@Mapper(componentModel = "spring")
public interface GoodsReceiptWebMapper {

    GoodsReceiptDetailResponse toDetailResponse(GoodsReceiptDetail detail);

    List<GoodsReceiptDetailResponse> toDetailResponses(List<GoodsReceiptDetail> details);

    @Mapping(target = "details", ignore = true)
    GoodsReceiptResponse toResponse(GoodsReceipt grn);

    /** Combines the GRN header with its detail lines into a single response. */
    default GoodsReceiptResponse toResponse(GoodsReceipt grn, List<GoodsReceiptDetail> details) {
        GoodsReceiptResponse response = toResponse(grn);
        response.setDetails(toDetailResponses(details));
        return response;
    }

    /** Request lines -> domain command value objects. */
    default List<GoodsReceiptLineCommand> toCommands(List<GoodsReceiptLineRequest> lines) {
        if (lines == null) {
            return Collections.emptyList();
        }
        return lines.stream()
                .map(line -> new GoodsReceiptLineCommand(
                        line.getProductId(),
                        line.getPoDetailId(),
                        line.getQuantity(),
                        line.getBatchNumber(),
                        line.getExpiryDate(),
                        line.getBinLocationId()))
                .collect(Collectors.toList());
    }
}
