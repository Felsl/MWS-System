package org.lvtn.mws.infrastructure.persistence.mapper;

import org.lvtn.mws.domain.model.AdjustmentVoucher;
import org.lvtn.mws.domain.model.AdjustmentVoucherDetail;
import org.lvtn.mws.infrastructure.persistence.entity.AdjustmentVoucherDetailEntity;
import org.lvtn.mws.infrastructure.persistence.entity.AdjustmentVoucherEntity;
import org.mapstruct.Mapper;

import java.util.ArrayList;
import java.util.List;

/** Aggregate mapper (header + details) theo mẫu SalesOrderInfraMapper. */
@Mapper(componentModel = "spring")
public interface AdjustmentVoucherInfraMapper {

    default AdjustmentVoucherEntity toEntity(AdjustmentVoucher d) {
        if (d == null) return null;
        AdjustmentVoucherEntity e = new AdjustmentVoucherEntity();
        e.setId(d.getId());
        e.setVoucherNumber(d.getVoucherNumber());
        e.setWarehouseId(d.getWarehouseId());
        e.setSessionId(d.getSessionId());
        e.setReason(d.getReason());
        e.setStatus(d.getStatus().name());
        e.setCreatedBy(d.getCreatedBy());
        e.setApprovedBy(d.getApprovedBy());
        e.setCreatedAt(d.getCreatedAt());

        List<AdjustmentVoucherDetailEntity> detailEntities = new ArrayList<>();
        for (AdjustmentVoucherDetail line : d.getDetails()) {
            AdjustmentVoucherDetailEntity de = toDetailEntity(line);
            de.setVoucher(e); // back-reference FK voucher_id
            detailEntities.add(de);
        }
        e.setDetails(detailEntities);
        return e;
    }

    default AdjustmentVoucherDetailEntity toDetailEntity(AdjustmentVoucherDetail d) {
        AdjustmentVoucherDetailEntity de = new AdjustmentVoucherDetailEntity();
        de.setId(d.getId());
        de.setProductId(d.getProductId());
        de.setBatchId(d.getBatchId());
        de.setBinLocationId(d.getBinLocationId());
        de.setQuantityChange(d.getQuantityChange());
        de.setBeforeQuantity(d.getBeforeQuantity());
        de.setAfterQuantity(d.getAfterQuantity());
        de.setStocktakeDetailId(d.getStocktakeDetailId());
        return de;
    }

    default AdjustmentVoucher toDomain(AdjustmentVoucherEntity e) {
        if (e == null) return null;
        List<AdjustmentVoucherDetail> details = new ArrayList<>();
        if (e.getDetails() != null) {
            for (AdjustmentVoucherDetailEntity de : e.getDetails()) {
                details.add(AdjustmentVoucherDetail.builder()
                        .id(de.getId())
                        .voucherId(e.getId())
                        .productId(de.getProductId())
                        .batchId(de.getBatchId())
                        .binLocationId(de.getBinLocationId())
                        .quantityChange(de.getQuantityChange())
                        .beforeQuantity(de.getBeforeQuantity())
                        .afterQuantity(de.getAfterQuantity())
                        .stocktakeDetailId(de.getStocktakeDetailId())
                        .build());
            }
        }
        return AdjustmentVoucher.builder()
                .id(e.getId())
                .voucherNumber(e.getVoucherNumber())
                .warehouseId(e.getWarehouseId())
                .sessionId(e.getSessionId())
                .reason(e.getReason())
                .status(AdjustmentVoucher.Status.valueOf(e.getStatus()))
                .createdBy(e.getCreatedBy())
                .approvedBy(e.getApprovedBy())
                .createdAt(e.getCreatedAt())
                .details(details)
                .build();
    }
}
