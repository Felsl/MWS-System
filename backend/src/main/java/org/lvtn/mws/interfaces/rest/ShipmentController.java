package org.lvtn.mws.interfaces.rest;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.application.usecases.shipment.AssignTrackingUseCase;
import org.lvtn.mws.application.usecases.shipment.CreateShipmentUseCase;
import org.lvtn.mws.application.usecases.shipment.DeliverShipmentUseCase;
import org.lvtn.mws.application.usecases.shipment.GetAllShipmentsUseCase;
import org.lvtn.mws.application.usecases.shipment.GetShipmentByIdUseCase;
import org.lvtn.mws.application.usecases.shipment.ShipShipmentUseCase;
import org.lvtn.mws.interfaces.dto.request.shipment.AssignTrackingRequest;
import org.lvtn.mws.interfaces.dto.request.shipment.CreateShipmentRequest;
import org.lvtn.mws.interfaces.dto.request.shipment.ShipShipmentRequest;
import org.lvtn.mws.interfaces.dto.response.shipment.ShipmentResponse;
import org.lvtn.mws.interfaces.mapper.ShipmentWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/shipments")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('OUTBOUND_VIEW')")
public class ShipmentController {

    private final CreateShipmentUseCase createShipmentUseCase;
    private final AssignTrackingUseCase assignTrackingUseCase;
    private final ShipShipmentUseCase shipShipmentUseCase;
    private final DeliverShipmentUseCase deliverShipmentUseCase;
    private final GetShipmentByIdUseCase getShipmentByIdUseCase;
    private final GetAllShipmentsUseCase getAllShipmentsUseCase;
    private final ShipmentWebMapper mapper;

    /** Tạo vận đơn (PACKED) cho đơn bán hàng đã gom xong. */
    @PreAuthorize("hasAuthority('OUTBOUND_SHIP')")
    @PostMapping
    public ResponseEntity<ShipmentResponse> create(@Valid @RequestBody CreateShipmentRequest req) {
        var s = createShipmentUseCase.execute(req.salesOrderId(), req.carrierId());
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(s));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShipmentResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(mapper.toResponse(getShipmentByIdUseCase.execute(id)));
    }

    @GetMapping
    public ResponseEntity<List<ShipmentResponse>> getAll() {
        return ResponseEntity.ok(mapper.toResponseList(getAllShipmentsUseCase.execute()));
    }

    /** Gán hãng vận chuyển + tracking number. */
    @PreAuthorize("hasAuthority('OUTBOUND_SHIP')")
    @PostMapping("/{id}/tracking")
    public ResponseEntity<ShipmentResponse> assignTracking(@PathVariable String id,
                                                           @Valid @RequestBody AssignTrackingRequest req) {
        return ResponseEntity.ok(mapper.toResponse(
                assignTrackingUseCase.execute(id, req.carrierId(), req.trackingNumber())));
    }

    /**
     * Xuất hàng (PACKED -> SHIPPING). Kích hoạt khấu trừ tồn kho song tầng,
     * ghi thẻ kho OUTBOUND và nâng SO -> SHIPPED trong cùng transaction.
     */
    @PreAuthorize("hasAuthority('OUTBOUND_SHIP')")
    @PostMapping("/{id}/ship")
    public ResponseEntity<ShipmentResponse> ship(@PathVariable String id,
                                                 @Valid @RequestBody ShipShipmentRequest req) {
        return ResponseEntity.ok(mapper.toResponse(
                shipShipmentUseCase.execute(id, req.actorUserId(), req.warehouseId())));
    }

    /** Giao thành công (SHIPPING -> DELIVERED). */
    @PreAuthorize("hasAuthority('OUTBOUND_SHIP')")
    @PostMapping("/{id}/deliver")
    public ResponseEntity<ShipmentResponse> deliver(@PathVariable String id) {
        return ResponseEntity.ok(mapper.toResponse(deliverShipmentUseCase.execute(id)));
    }
}
