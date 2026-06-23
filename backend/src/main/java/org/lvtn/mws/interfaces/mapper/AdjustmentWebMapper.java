package org.lvtn.mws.interfaces.mapper;

import org.lvtn.mws.domain.model.AdjustmentVoucher;
import org.lvtn.mws.domain.model.AdjustmentVoucherDetail;
import org.lvtn.mws.interfaces.dto.response.adjustment.AdjustmentVoucherDetailResponse;
import org.lvtn.mws.interfaces.dto.response.adjustment.AdjustmentVoucherResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AdjustmentWebMapper {

    default AdjustmentVoucherDetailResponse toDetailResponse(AdjustmentVoucherDetail d) {
        if (d == null) return null;
        return new AdjustmentVoucherDetailResponse(
                d.getId(), d.getVoucherId(), d.getProductId(), d.getBatchId(), d.getBinLocationId(),
                d.getQuantityChange(), d.getBeforeQuantity(), d.getAfterQuantity(),
                d.getStocktakeDetailId());
    }

    default AdjustmentVoucherResponse toResponse(AdjustmentVoucher v) {
        if (v == null) return null;
        List<AdjustmentVoucherDetailResponse> details =
                v.getDetails().stream().map(this::toDetailResponse).toList();
        return new AdjustmentVoucherResponse(
                v.getId(), v.getVoucherNumber(), v.getWarehouseId(), v.getSessionId(),
                v.getReason(),
                v.getStatus() == null ? null : v.getStatus().name(),
                v.getCreatedBy(), v.getApprovedBy(), v.getCreatedAt(),
                v.maxVariancePercent(), details);
    }

    default List<AdjustmentVoucherResponse> toResponseList(List<AdjustmentVoucher> list) {
        if (list == null) return List.of();
        return list.stream().map(this::toResponse).toList();
    }
}
