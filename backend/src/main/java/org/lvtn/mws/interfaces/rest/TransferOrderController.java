package org.lvtn.mws.interfaces.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.lvtn.mws.application.usecases.transfer.ApproveTransferOrderUseCase;
import org.lvtn.mws.application.usecases.transfer.CompleteTransferReceiptUseCase;
import org.lvtn.mws.application.usecases.transfer.CreateTransferOrderUseCase;
import org.lvtn.mws.application.usecases.transfer.DispatchTransferShipmentUseCase;
import org.lvtn.mws.application.usecases.transfer.GetAllTransferOrdersUseCase;
import org.lvtn.mws.application.usecases.transfer.GetTransferOrderByIdUseCase;
import org.lvtn.mws.application.usecases.transfer.RequestTransferApprovalUseCase;
import org.lvtn.mws.domain.model.Shipment;
import org.lvtn.mws.domain.model.TransferOrder;
import org.lvtn.mws.interfaces.dto.request.transfer.ApproveTransferRequest;
import org.lvtn.mws.interfaces.dto.request.transfer.CompleteReceiptRequest;
import org.lvtn.mws.interfaces.dto.request.transfer.CreateTransferOrderRequest;
import org.lvtn.mws.interfaces.dto.request.transfer.DispatchTransferRequest;
import org.lvtn.mws.interfaces.dto.response.transfer.ShipmentResponse;
import org.lvtn.mws.interfaces.dto.response.transfer.TransferOrderResponse;
import org.lvtn.mws.interfaces.mapper.TransferWebMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST API điều chuyển nội bộ. Vòng đời:
 * create(DRAFT) -> request-approval(REQUESTED) -> approve(APPROVED, FEFO)
 * -> dispatch(IN_TRANSIT, tạo shipment + trừ kho nguồn) -> complete(COMPLETED, cộng kho đích).
 * Lỗi nghiệp vụ (IllegalArgument/IllegalState/InsufficientStock) do GlobalExceptionHandler
 * dùng chung của dự án xử lý -> trả 400.
 */
@RestController
@RequestMapping("/api/v1/transfer-orders")
@RequiredArgsConstructor
public class TransferOrderController {

    private final CreateTransferOrderUseCase createTransferOrderUseCase;
    private final RequestTransferApprovalUseCase requestTransferApprovalUseCase;
    private final ApproveTransferOrderUseCase approveTransferOrderUseCase;
    private final DispatchTransferShipmentUseCase dispatchTransferShipmentUseCase;
    private final CompleteTransferReceiptUseCase completeTransferReceiptUseCase;
    private final GetTransferOrderByIdUseCase getTransferOrderByIdUseCase;
    private final GetAllTransferOrdersUseCase getAllTransferOrdersUseCase;
    private final TransferWebMapper webMapper;

    /** I. Tạo phiếu DRAFT. */
    @PostMapping
    public ResponseEntity<TransferOrderResponse> create(@Valid @RequestBody CreateTransferOrderRequest request) {
        TransferOrder order = createTransferOrderUseCase.execute(
                request.getFromWarehouseId(),
                request.getToWarehouseId(),
                request.getCreatedBy(),
                webMapper.toNewTransferLines(request.getLines()));
        return ResponseEntity.status(HttpStatus.CREATED).body(webMapper.toResponse(order));
    }

    /** I.2 Gửi duyệt + giữ chỗ ảo kho nguồn (DRAFT -> REQUESTED). */
    @PostMapping("/{id}/request-approval")
    public ResponseEntity<TransferOrderResponse> requestApproval(@PathVariable("id") String id) {
        TransferOrder order = requestTransferApprovalUseCase.execute(id);
        return ResponseEntity.ok(webMapper.toResponse(order));
    }

    /** II.1 Duyệt + FEFO gán lô/ô kệ nguồn (REQUESTED -> APPROVED). */
    @PostMapping("/{id}/approve")
    public ResponseEntity<TransferOrderResponse> approve(@PathVariable("id") String id,
                                                         @Valid @RequestBody ApproveTransferRequest request) {
        TransferOrder order = approveTransferOrderUseCase.execute(id, request.getApprovedBy());
        return ResponseEntity.ok(webMapper.toResponse(order));
    }

    /** II.2 Đóng gói + tạo giao vận + trừ kho nguồn (APPROVED -> IN_TRANSIT). */
    @PostMapping("/{id}/dispatch")
    public ResponseEntity<ShipmentResponse> dispatch(@PathVariable("id") String id,
                                                     @Valid @RequestBody DispatchTransferRequest request) {
        Shipment shipment = dispatchTransferShipmentUseCase.execute(id, request.getCarrierId());
        return ResponseEntity.ok(webMapper.toShipmentResponse(shipment));
    }

    /** III + IV Cập bến + đối soát + cộng kho đích + thẻ kho (IN_TRANSIT -> COMPLETED). */
    @PostMapping("/{id}/complete")
    public ResponseEntity<TransferOrderResponse> complete(@PathVariable("id") String id,
                                                          @Valid @RequestBody CompleteReceiptRequest request) {
        TransferOrder order = completeTransferReceiptUseCase.execute(id, webMapper.toReceiptLines(request.getLines()));
        return ResponseEntity.ok(webMapper.toResponse(order));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransferOrderResponse> getById(@PathVariable("id") String id) {
        return ResponseEntity.ok(webMapper.toResponse(getTransferOrderByIdUseCase.execute(id)));
    }

    @GetMapping
    public ResponseEntity<List<TransferOrderResponse>> getAll() {
        return ResponseEntity.ok(webMapper.toResponseList(getAllTransferOrdersUseCase.execute()));
    }
}
