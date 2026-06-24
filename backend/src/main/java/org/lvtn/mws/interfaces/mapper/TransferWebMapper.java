package org.lvtn.mws.interfaces.mapper;

import org.lvtn.mws.domain.model.NewTransferLine;
import org.lvtn.mws.domain.model.Shipment;
import org.lvtn.mws.domain.model.TransferOrder;
import org.lvtn.mws.domain.model.TransferOrderDetail;
import org.lvtn.mws.domain.model.TransferReceiptLine;
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

    // ── request -> domain input ────────────────────────────────────────────────

    public List<NewTransferLine> toNewTransferLines(List<TransferLineItemRequest> reqs) {
        List<NewTransferLine> lines = new ArrayList<>();
        if (reqs != null) {
            for (TransferLineItemRequest r : reqs) {
                lines.add(new NewTransferLine(r.getProductId(), r.getQuantity()));
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
                .toWarehouseId(order.getToWarehouseId())
                .transferNumber(order.getTransferNumber())
                .status(order.getStatus() != null ? order.getStatus().name() : null)
                .createdBy(order.getCreatedBy())
                .approvedBy(order.getApprovedBy())
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
                .quantity(d.getQuantity())
                .quantityReceived(d.getQuantityReceived())
                .lostQuantity(d.lostQuantity())
                .fromBinLocationId(d.getFromBinLocationId())
                .binLocationId(d.getBinLocationId())
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
