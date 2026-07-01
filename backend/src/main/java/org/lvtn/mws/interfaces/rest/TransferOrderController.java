package org.lvtn.mws.interfaces.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.lvtn.mws.application.usecases.transfer.ApproveTransferOrderUseCase;
import org.lvtn.mws.application.usecases.transfer.CancelTransferOrderUseCase;
import org.lvtn.mws.application.usecases.transfer.CompleteTransferReceiptUseCase;
import org.lvtn.mws.application.usecases.transfer.CreateTransferOrderUseCase;
import org.lvtn.mws.application.usecases.transfer.DispatchTransferShipmentUseCase;
import org.lvtn.mws.application.usecases.transfer.GetAllTransferOrdersUseCase;
import org.lvtn.mws.application.usecases.transfer.GetTransferOrderByIdUseCase;
import org.lvtn.mws.application.usecases.transfer.RejectTransferOrderUseCase;
import org.lvtn.mws.application.usecases.transfer.RequestTransferApprovalUseCase;
import org.lvtn.mws.domain.model.Shipment;
import org.lvtn.mws.domain.model.TransferOrder;
import org.lvtn.mws.interfaces.dto.request.transfer.ApproveTransferRequest;
import org.lvtn.mws.interfaces.dto.request.transfer.CompleteReceiptRequest;
import org.lvtn.mws.interfaces.dto.request.transfer.CreateTransferOrderRequest;
import org.lvtn.mws.interfaces.dto.request.transfer.DispatchTransferRequest;
import org.lvtn.mws.interfaces.dto.request.transfer.RejectTransferRequest;
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
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * REST API điều chuyển nội bộ. Vòng đời:
 * create(DRAFT) -> request-approval(PENDING_APPROVAL) -> approve(APPROVED, FEFO)
 * -> dispatch(IN_TRANSIT, tạo shipment + trừ kho nguồn) -> complete(COMPLETED, cộng kho đích).
 * Lỗi nghiệp vụ (IllegalArgument/IllegalState/InsufficientStock) do GlobalExceptionHandler
 * dùng chung của dự án xử lý -> trả 400.
 */
@RestController
@RequestMapping("/api/v1/transfer-orders")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('TRANSFER_VIEW')")
public class TransferOrderController {

    private final CreateTransferOrderUseCase createTransferOrderUseCase;
    private final RequestTransferApprovalUseCase requestTransferApprovalUseCase;
    private final ApproveTransferOrderUseCase approveTransferOrderUseCase;
    private final RejectTransferOrderUseCase rejectTransferOrderUseCase;
    private final CancelTransferOrderUseCase cancelTransferOrderUseCase;
    private final DispatchTransferShipmentUseCase dispatchTransferShipmentUseCase;
    private final CompleteTransferReceiptUseCase completeTransferReceiptUseCase;
    private final GetTransferOrderByIdUseCase getTransferOrderByIdUseCase;
    private final GetAllTransferOrdersUseCase getAllTransferOrdersUseCase;
    private final TransferWebMapper webMapper;

    /** I. Tạo phiếu DRAFT. */
    @PreAuthorize("hasAuthority('TRANSFER_CREATE')")
    @PostMapping
    public ResponseEntity<TransferOrderResponse> create(@Valid @RequestBody CreateTransferOrderRequest request) {
        TransferOrder order = createTransferOrderUseCase.execute(
                request.getFromWarehouseId(),
                request.getToWarehouseId(),
                request.getCreatedBy(),
                webMapper.toNewTransferLines(request.getLines()));
        return ResponseEntity.status(HttpStatus.CREATED).body(webMapper.toResponse(order));
    }

    /** I.2 Gửi duyệt + giữ chỗ ảo kho nguồn (DRAFT -> PENDING_APPROVAL). */
    @PreAuthorize("hasAuthority('TRANSFER_CREATE')")
    @PostMapping("/{id}/request-approval")
    public ResponseEntity<TransferOrderResponse> requestApproval(@PathVariable("id") String id) {
        TransferOrder order = requestTransferApprovalUseCase.execute(id);
        return ResponseEntity.ok(webMapper.toResponse(order));
    }

    /** II.1 Duyệt + FEFO gán lô/ô kệ nguồn (PENDING_APPROVAL -> APPROVED). */
    @PreAuthorize("hasAuthority('TRANSFER_APPROVE')")
    @PostMapping("/{id}/approve")
    public ResponseEntity<TransferOrderResponse> approve(@PathVariable("id") String id,
                                                         @Valid @RequestBody ApproveTransferRequest request) {
        TransferOrder order = approveTransferOrderUseCase.execute(id, request.getApprovedBy());
        return ResponseEntity.ok(webMapper.toResponse(order));
    }

    /** Từ chối duyệt (PENDING_APPROVAL -> REJECTED). Nhả lại hàng giữ chỗ kho nguồn. */
    @PreAuthorize("hasAuthority('TRANSFER_APPROVE')")
    @PostMapping("/{id}/reject")
    public ResponseEntity<TransferOrderResponse> reject(@PathVariable("id") String id,
                                                        @Valid @RequestBody RejectTransferRequest request) {
        TransferOrder order = rejectTransferOrderUseCase.execute(id, request.getRejectedBy());
        return ResponseEntity.ok(webMapper.toResponse(order));
    }

    /** Huỷ phiếu (DRAFT/PENDING_APPROVAL/APPROVED -> CANCELLED). Nhả lại hàng giữ chỗ nếu có. */
    @PreAuthorize("hasAuthority('TRANSFER_CREATE')")
    @PostMapping("/{id}/cancel")
    public ResponseEntity<TransferOrderResponse> cancel(@PathVariable("id") String id) {
        TransferOrder order = cancelTransferOrderUseCase.execute(id);
        return ResponseEntity.ok(webMapper.toResponse(order));
    }

    /** II.2 Đóng gói + tạo giao vận + trừ kho nguồn (APPROVED -> IN_TRANSIT). */
    @PreAuthorize("hasAuthority('TRANSFER_DISPATCH')")
    @PostMapping("/{id}/dispatch")
    public ResponseEntity<ShipmentResponse> dispatch(@PathVariable("id") String id,
                                                     @Valid @RequestBody DispatchTransferRequest request) {
        Shipment shipment = dispatchTransferShipmentUseCase.execute(id, request.getCarrierId());
        return ResponseEntity.ok(webMapper.toShipmentResponse(shipment));
    }

    /** III + IV Cập bến + đối soát + cộng kho đích + thẻ kho (IN_TRANSIT -> COMPLETED). */
    @PreAuthorize("hasAuthority('TRANSFER_RECEIVE')")
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
