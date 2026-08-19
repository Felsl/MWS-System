package org.lvtn.mws.interfaces.mapper;

import org.lvtn.mws.domain.model.StocktakeDetail;
import org.lvtn.mws.domain.model.StocktakeSession;
import org.lvtn.mws.domain.model.User;
import org.lvtn.mws.domain.repository.IInventoryBatchRepository;
import org.lvtn.mws.domain.repository.IUserRepository;
import org.lvtn.mws.interfaces.dto.response.stocktake.StocktakeDetailResponse;
import org.lvtn.mws.interfaces.dto.response.stocktake.StocktakeResponse;
import org.lvtn.mws.interfaces.dto.response.stocktake.StocktakeSessionResponse;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Chuyển domain -> DTO cho kiểm kê. Là @Component (không dùng MapStruct) để inject repo,
 * resolve id -> tên/mã ngay ở BE. FE không còn phụ thuộc bảng lookup phía client (vốn có
 * thể rỗng với user phạm vi kho hẹp trên mobile).
 */
@Component
public class StocktakeWebMapper {

    private final IUserRepository userRepository;
    private final IInventoryBatchRepository batchRepository;

    public StocktakeWebMapper(IUserRepository userRepository, IInventoryBatchRepository batchRepository) {
        this.userRepository = userRepository;
        this.batchRepository = batchRepository;
    }

    private String userName(String id) {
        return id == null ? null : userRepository.findById(id).map(User::getUsername).orElse(id);
    }
    private String batchNumber(String id) {
        return id == null ? null : batchRepository.findById(id).map(b -> b.getBatchNumber()).orElse(id);
    }

    public StocktakeSessionResponse toSessionResponse(StocktakeSession s) {
        if (s == null) return null;
        return new StocktakeSessionResponse(
                s.getId(), s.getWarehouseId(),
                s.getStatus() == null ? null : s.getStatus().name(),
                s.getFreezeStartedAt(), s.getFreezeEndedAt(),
                s.getCreatedBy(), userName(s.getCreatedBy()), s.getCreatedAt());
    }

    public StocktakeDetailResponse toDetailResponse(StocktakeDetail d) {
        if (d == null) return null;
        return new StocktakeDetailResponse(
                d.getId(), d.getSessionId(), d.getProductId(), d.getBinLocationId(),
                d.getBatchId(), batchNumber(d.getBatchId()),
                d.getSystemQuantity(), d.getCountedQuantity(), d.getDifference(),
                d.getAdjustmentReason(), d.getCountedBy(), d.getCountedAt(),
                d.getApprovedBy(), d.getApprovedAt());
    }

    public List<StocktakeDetailResponse> toDetailResponseList(List<StocktakeDetail> list) {
        if (list == null) return List.of();
        return list.stream().map(this::toDetailResponse).toList();
    }

    public StocktakeResponse toResponse(StocktakeSession session, List<StocktakeDetail> details) {
        return new StocktakeResponse(toSessionResponse(session), toDetailResponseList(details));
    }
}
