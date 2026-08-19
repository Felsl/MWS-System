package org.lvtn.mws.interfaces.mapper;

import org.lvtn.mws.domain.model.NewTransferLine;
import org.lvtn.mws.domain.model.Shipment;
import org.lvtn.mws.domain.model.TransferOrder;
import org.lvtn.mws.domain.model.TransferOrderDetail;
import org.lvtn.mws.domain.model.TransferReceiptLine;
import org.lvtn.mws.domain.model.User;
import org.lvtn.mws.domain.repository.IBinLocationRepository;
import org.lvtn.mws.domain.repository.IInventoryBatchRepository;
import org.lvtn.mws.domain.repository.IPickingListRepository;
import org.lvtn.mws.domain.repository.IUserRepository;
import org.lvtn.mws.domain.repository.IWarehouseRepository;
import org.lvtn.mws.interfaces.dto.request.transfer.TransferLineItemRequest;
import org.lvtn.mws.interfaces.dto.request.transfer.TransferReceiptLineRequest;
import org.lvtn.mws.interfaces.dto.response.transfer.ShipmentResponse;
import org.lvtn.mws.interfaces.dto.response.transfer.TransferOrderDetailResponse;
import org.lvtn.mws.interfaces.dto.response.transfer.TransferOrderResponse;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Chuyển đổi giữa DTO tầng web và domain model cho luồng điều chuyển.
 * Để thuần POJO + @Component (không dùng MapStruct vì có logic tách field lostQuantity).
 */
@Component
public class TransferWebMapper {

    private final IUserRepository userRepository;
    private final IWarehouseRepository warehouseRepository;
    private final IBinLocationRepository binLocationRepository;
    private final IInventoryBatchRepository batchRepository;
    private final IPickingListRepository pickingListRepository;

    public TransferWebMapper(IUserRepository userRepository, IWarehouseRepository warehouseRepository,
                             IBinLocationRepository binLocationRepository, IInventoryBatchRepository batchRepository,
                             IPickingListRepository pickingListRepository) {
        this.userRepository = userRepository;
        this.warehouseRepository = warehouseRepository;
        this.binLocationRepository = binLocationRepository;
        this.batchRepository = batchRepository;
        this.pickingListRepository = pickingListRepository;
    }

    // Resolve id -> tên hiển thị (fallback về id nếu không tìm thấy) để FE không phải
    // tra bảng lookup phía client (vốn có thể rỗng với user phạm vi kho hẹp).
    private String userName(String id) {
        return id == null ? null : userRepository.findById(id).map(User::getUsername).orElse(id);
    }
    private String warehouseName(String id) {
        return id == null ? null : warehouseRepository.findById(id).map(w -> w.getName()).orElse(id);
    }
    private String binLabel(String id) {
        return id == null ? null : binLocationRepository.findById(id).map(b -> b.locationCode()).orElse(id);
    }
    private String batchNumber(String id) {
        return id == null ? null : batchRepository.findById(id).map(b -> b.getBatchNumber()).orElse(id);
    }

    // ── request -> domain input ────────────────────────────────────────────────

    public List<NewTransferLine> toNewTransferLines(List<TransferLineItemRequest> reqs) {
        List<NewTransferLine> lines = new ArrayList<>();
        if (reqs != null) {
            for (TransferLineItemRequest r : reqs) {
                lines.add(new NewTransferLine(r.getProductId(), r.getQuantity(),
                        r.getDesignatedBatchId(), r.getSupplierId(), r.getBatchMode()));
            }
        }
        return lines;
    }

    public List<TransferReceiptLine> toReceiptLines(List<TransferReceiptLineRequest> reqs) {
        List<TransferReceiptLine> lines = new ArrayList<>();
        if (reqs != null) {
            for (TransferReceiptLineRequest r : reqs) {
                lines.add(new TransferReceiptLine(r.getDetailId(), r.getQuantityReceived(), r.getBinLocationId()));
            }
        }
        return lines;
    }

    // ── domain -> response ─────────────────────────────────────────────────────

    public TransferOrderResponse toResponse(TransferOrder order) {
        if (order == null) return null;
        List<TransferOrderDetailResponse> details = new ArrayList<>();
        for (TransferOrderDetail d : order.getDetails()) {
            details.add(toDetailResponse(d));
        }
        return TransferOrderResponse.builder()
                .id(order.getId())
                .fromWarehouseId(order.getFromWarehouseId())
                .fromWarehouseName(warehouseName(order.getFromWarehouseId()))
                .toWarehouseId(order.getToWarehouseId())
                .toWarehouseName(warehouseName(order.getToWarehouseId()))
                .transferNumber(order.getTransferNumber())
                .status(order.getStatus() != null ? order.getStatus().name() : null)
                .createdBy(order.getCreatedBy())
                .createdByName(userName(order.getCreatedBy()))
                .approvedBy(order.getApprovedBy())
                .approvedByName(userName(order.getApprovedBy()))
                // Id lệnh lấy hàng dùng chung (nếu đã tạo) để FE mở thẳng trong "Lệnh lấy hàng".
                .pickingListId(pickingListRepository.findByTransferOrderId(order.getId()).map(pl -> pl.getId()).orElse(null))
                .approvedAt(order.getApprovedAt())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .details(details)
                .build();
    }

    public TransferOrderDetailResponse toDetailResponse(TransferOrderDetail d) {
        if (d == null) return null;
        return TransferOrderDetailResponse.builder()
                .id(d.getId())
                .transferOrderId(d.getTransferOrderId())
                .productId(d.getProductId())
                .batchId(d.getBatchId())
                .batchNumber(batchNumber(d.getBatchId()))
                .designatedBatchId(d.getDesignatedBatchId())
                .supplierId(d.getSupplierId())
                .batchMode(d.getBatchMode())
                .quantity(d.getQuantity())
                .quantityReceived(d.getQuantityReceived())
                .lostQuantity(d.lostQuantity())
                .fromBinLocationId(d.getFromBinLocationId())
                .fromBinLocationLabel(binLabel(d.getFromBinLocationId()))
                .binLocationId(d.getBinLocationId())
                .binLocationLabel(binLabel(d.getBinLocationId()))
                .build();
    }

    public ShipmentResponse toShipmentResponse(Shipment s) {
        if (s == null) return null;
        return ShipmentResponse.builder()
                .id(s.getId())
                .shipmentNumber(s.getShipmentNumber())
                .salesOrderId(s.getSalesOrderId())
                .transferOrderId(s.getTransferOrderId())
                .carrierId(s.getCarrierId())
                .trackingNumber(s.getTrackingNumber())
                .status(s.getStatus() != null ? s.getStatus().name() : null)
                .shippedAt(s.getShippedAt())
                .deliveredAt(s.getDeliveredAt())
                .createdAt(s.getCreatedAt())
                .build();
    }

    public List<TransferOrderResponse> toResponseList(List<TransferOrder> orders) {
        List<TransferOrderResponse> list = new ArrayList<>();
        if (orders != null) {
            for (TransferOrder o : orders) list.add(toResponse(o));
        }
        return list;
    }
}
