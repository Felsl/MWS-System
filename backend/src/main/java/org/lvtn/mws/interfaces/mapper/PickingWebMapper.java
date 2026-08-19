package org.lvtn.mws.interfaces.mapper;

import org.lvtn.mws.domain.model.PickingList;
import org.lvtn.mws.domain.model.PickingListDetail;
import org.lvtn.mws.domain.model.User;
import org.lvtn.mws.domain.repository.IBinLocationRepository;
import org.lvtn.mws.domain.repository.IInventoryBatchRepository;
import org.lvtn.mws.domain.repository.ISalesOrderRepository;
import org.lvtn.mws.domain.repository.ITransferOrderRepository;
import org.lvtn.mws.domain.repository.IUserRepository;
import org.lvtn.mws.interfaces.dto.response.picking.PickingListDetailResponse;
import org.lvtn.mws.interfaces.dto.response.picking.PickingListResponse;
import org.springframework.stereotype.Component;

import java.util.List;

/** Domain -> Response. Resolve người lấy (userId -> username) và lô cần (batchId -> batchNumber). */
@Component
public class PickingWebMapper {

    private final IUserRepository userRepository;
    private final IInventoryBatchRepository batchRepository;
    private final IBinLocationRepository binLocationRepository;
    private final ISalesOrderRepository salesOrderRepository;
    private final ITransferOrderRepository transferOrderRepository;

    public PickingWebMapper(IUserRepository userRepository, IInventoryBatchRepository batchRepository,
                            IBinLocationRepository binLocationRepository, ISalesOrderRepository salesOrderRepository,
                            ITransferOrderRepository transferOrderRepository) {
        this.userRepository = userRepository;
        this.batchRepository = batchRepository;
        this.binLocationRepository = binLocationRepository;
        this.salesOrderRepository = salesOrderRepository;
        this.transferOrderRepository = transferOrderRepository;
    }

    public PickingListResponse toResponse(PickingList pl) {
        if (pl == null) return null;
        String assignedToName = pl.getAssignedTo() == null ? null
                : userRepository.findById(pl.getAssignedTo()).map(User::getUsername).orElse(pl.getAssignedTo());
        // Resolve số đơn bán (soId -> soNumber) để FE không phải tra bảng phía client.
        String soNumber = pl.getSoId() == null ? null
                : salesOrderRepository.findById(pl.getSoId()).map(so -> so.getSoNumber()).orElse(pl.getSoId());
        // Resolve số phiếu điều chuyển cho lệnh gom của điều chuyển (transferOrderId -> transferNumber).
        String transferNumber = pl.getTransferOrderId() == null ? null
                : transferOrderRepository.findById(pl.getTransferOrderId()).map(o -> o.getTransferNumber()).orElse(pl.getTransferOrderId());
        return new PickingListResponse(
                pl.getId(),
                pl.getPickNumber(),
                pl.getSoId(),
                soNumber,
                pl.getTransferOrderId(),
                transferNumber,
                pl.getAssignedTo(),
                assignedToName,
                pl.getStatus() == null ? null : pl.getStatus().name(),
                pl.getStartedAt(),
                pl.getCompletedAt(),
                toDetailResponseList(pl.getDetails()));
    }

    public PickingListDetailResponse toDetailResponse(PickingListDetail d) {
        if (d == null) return null;
        String batchNumber = d.getBatchId() == null ? null
                : batchRepository.findById(d.getBatchId()).map(b -> b.getBatchNumber()).orElse(d.getBatchId());
        // Resolve nhãn ô kệ (binLocationId -> "A-1-1-5") server-side: FE không còn phụ thuộc
        // vào việc nạp danh sách kho/bins ở client (vốn có thể rỗng với user phạm vi hẹp).
        String binLocationLabel = d.getBinLocationId() == null ? null
                : binLocationRepository.findById(d.getBinLocationId()).map(b -> b.locationCode()).orElse(d.getBinLocationId());
        return new PickingListDetailResponse(
                d.getId(), d.getPickingListId(), d.getProductId(),
                d.getBatchId(), batchNumber, d.getRequiredBatchId(), d.getActualBatchId(),
                d.getBinLocationId(), binLocationLabel,
                d.getQuantityToPick(), d.getQuantityPicked(),
                d.isConfirmed(), d.getConfirmedBy(), d.getConfirmedAt());
    }

    public List<PickingListDetailResponse> toDetailResponseList(List<PickingListDetail> list) {
        if (list == null) return List.of();
        return list.stream().map(this::toDetailResponse).toList();
    }

    public List<PickingListResponse> toResponseList(List<PickingList> list) {
        if (list == null) return List.of();
        return list.stream().map(this::toResponse).toList();
    }
}
