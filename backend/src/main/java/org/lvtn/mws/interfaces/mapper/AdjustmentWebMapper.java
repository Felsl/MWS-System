package org.lvtn.mws.interfaces.mapper;

import org.lvtn.mws.domain.model.AdjustmentVoucher;
import org.lvtn.mws.domain.model.AdjustmentVoucherDetail;
import org.lvtn.mws.domain.model.User;
import org.lvtn.mws.domain.repository.IInventoryBatchRepository;
import org.lvtn.mws.domain.repository.IUserRepository;
import org.lvtn.mws.interfaces.dto.response.adjustment.AdjustmentVoucherDetailResponse;
import org.lvtn.mws.interfaces.dto.response.adjustment.AdjustmentVoucherResponse;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Chuyển domain -> DTO cho phiếu điều chỉnh tồn. @Component để inject repo và resolve
 * id -> tên/mã ngay ở BE (createdBy/approvedBy -> username, batchId -> batchNumber).
 */
@Component
public class AdjustmentWebMapper {

    private final IUserRepository userRepository;
    private final IInventoryBatchRepository batchRepository;

    public AdjustmentWebMapper(IUserRepository userRepository, IInventoryBatchRepository batchRepository) {
        this.userRepository = userRepository;
        this.batchRepository = batchRepository;
    }

    private String userName(String id) {
        return id == null ? null : userRepository.findById(id).map(User::getUsername).orElse(id);
    }
    private String batchNumber(String id) {
        return id == null ? null : batchRepository.findById(id).map(b -> b.getBatchNumber()).orElse(id);
    }

    public AdjustmentVoucherDetailResponse toDetailResponse(AdjustmentVoucherDetail d) {
        if (d == null) return null;
        return new AdjustmentVoucherDetailResponse(
                d.getId(), d.getVoucherId(), d.getProductId(),
                d.getBatchId(), batchNumber(d.getBatchId()), d.getBinLocationId(),
                d.getQuantityChange(), d.getBeforeQuantity(), d.getAfterQuantity(),
                d.getStocktakeDetailId());
    }

    public AdjustmentVoucherResponse toResponse(AdjustmentVoucher v) {
        if (v == null) return null;
        List<AdjustmentVoucherDetailResponse> details =
                v.getDetails().stream().map(this::toDetailResponse).toList();
        return new AdjustmentVoucherResponse(
                v.getId(), v.getVoucherNumber(), v.getWarehouseId(), v.getSessionId(),
                v.getReason(),
                v.getStatus() == null ? null : v.getStatus().name(),
                v.getCreatedBy(), userName(v.getCreatedBy()),
                v.getApprovedBy(), userName(v.getApprovedBy()), v.getCreatedAt(),
                v.maxVariancePercent(), details);
    }

    public List<AdjustmentVoucherResponse> toResponseList(List<AdjustmentVoucher> list) {
        if (list == null) return List.of();
        return list.stream().map(this::toResponse).toList();
    }
}
