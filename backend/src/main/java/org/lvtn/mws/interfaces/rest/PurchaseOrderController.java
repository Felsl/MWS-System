package org.lvtn.mws.interfaces.rest;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.application.usecases.purchaseorder.ApprovePurchaseOrderUseCase;
import org.lvtn.mws.application.usecases.purchaseorder.CreatePurchaseOrderUseCase;
import org.lvtn.mws.application.usecases.purchaseorder.GetPurchaseOrderByIdUseCase;
import org.lvtn.mws.application.usecases.purchaseorder.GetPurchaseOrderDetailsUseCase;
import org.lvtn.mws.application.usecases.purchaseorder.RejectPurchaseOrderUseCase;
import org.lvtn.mws.application.usecases.purchaseorder.SubmitPurchaseOrderForApprovalUseCase;
import org.lvtn.mws.application.usecases.purchaseorder.SubmitPurchaseOrderForReviewUseCase;
import org.lvtn.mws.application.usecases.purchaseorder.UpdatePurchaseOrderUseCase;
import org.lvtn.mws.domain.model.PurchaseOrder;
import org.lvtn.mws.interfaces.dto.request.purchaseorder.CreatePurchaseOrderRequest;
import org.lvtn.mws.interfaces.dto.response.purchaseorder.PurchaseOrderResponse;
import org.lvtn.mws.interfaces.mapper.PurchaseOrderWebMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/purchase-orders")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('INBOUND_VIEW_PO')")
public class PurchaseOrderController {

    private final CreatePurchaseOrderUseCase createUseCase;
    private final SubmitPurchaseOrderForReviewUseCase submitForReviewUseCase;
    private final SubmitPurchaseOrderForApprovalUseCase submitForApprovalUseCase;
    private final ApprovePurchaseOrderUseCase approveUseCase;
    private final RejectPurchaseOrderUseCase rejectUseCase;
    private final UpdatePurchaseOrderUseCase updateUseCase;
    private final GetPurchaseOrderByIdUseCase getByIdUseCase;
    private final GetPurchaseOrderDetailsUseCase getDetailsUseCase;
    private final org.lvtn.mws.application.usecases.purchaseorder.GetAllPurchaseOrdersUseCase getAllUseCase;
    private final PurchaseOrderWebMapper webMapper;

    @PreAuthorize("hasAuthority('INBOUND_CREATE_PO')")
    @PostMapping
    public ResponseEntity<PurchaseOrderResponse> create(
            @Valid @RequestBody CreatePurchaseOrderRequest request,
            Authentication authentication) {
        PurchaseOrder po = createUseCase.execute(
                request.getSupplierId(),
                request.getWarehouseId(),
                request.getExpectedDate(),
                authentication.getName(),
                webMapper.toCommands(request.getLines()));
        return ResponseEntity.status(HttpStatus.CREATED).body(buildResponse(po));
    }

    /** Sửa đơn mua chưa duyệt — CHỈ ADMIN. Trả về đơn sau khi sửa (kèm dòng chi tiết). */
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<PurchaseOrderResponse> update(
            @PathVariable String id,
            @Valid @RequestBody CreatePurchaseOrderRequest request) {
        PurchaseOrder po = updateUseCase.execute(
                id,
                request.getSupplierId(),
                request.getWarehouseId(),
                request.getExpectedDate(),
                webMapper.toCommands(request.getLines()));
        return ResponseEntity.ok(buildResponse(po));
    }

    @PreAuthorize("hasAuthority('INBOUND_CREATE_PO')")
    @PostMapping("/{id}/submit-review")
    public ResponseEntity<PurchaseOrderResponse> submitForReview(@PathVariable String id) {
        return ResponseEntity.ok(buildResponse(submitForReviewUseCase.execute(id)));
    }

    @PreAuthorize("hasAuthority('INBOUND_SUBMIT_PO')")
    @PostMapping("/{id}/submit-approval")
    public ResponseEntity<PurchaseOrderResponse> submitForApproval(@PathVariable String id) {
        return ResponseEntity.ok(buildResponse(submitForApprovalUseCase.execute(id)));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('INBOUND_APPROVE_PO')")
    public ResponseEntity<PurchaseOrderResponse> approve(
            @PathVariable String id,
            Authentication authentication) {
        String approvedBy = authentication.getName();
        return ResponseEntity.ok(buildResponse(approveUseCase.execute(id, approvedBy)));
    }

    @PreAuthorize("hasAuthority('INBOUND_APPROVE_PO')")
    @PostMapping("/{id}/reject")
    public ResponseEntity<PurchaseOrderResponse> reject(@PathVariable String id) {
        return ResponseEntity.ok(buildResponse(rejectUseCase.execute(id)));
    }

    /** [B4] Danh sách PO có tìm kiếm + lọc + phân trang (header-only, không kèm dòng chi tiết). */
    @PreAuthorize("hasAuthority('INBOUND_VIEW_PO')")
    @GetMapping
    public ResponseEntity<org.lvtn.mws.interfaces.dto.response.common.PageResponse<PurchaseOrderResponse>> getAll(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String dir) {
        var result = getAllUseCase.execute(keyword, status, page, size, sort, dir);
        return ResponseEntity.ok(
                org.lvtn.mws.interfaces.dto.response.common.PageResponse.from(result, webMapper::toResponse));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PurchaseOrderResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(buildResponse(getByIdUseCase.execute(id)));
    }

    /** Assembles the full response (header + detail lines) for a purchase order. */
    private PurchaseOrderResponse buildResponse(PurchaseOrder po) {
        return webMapper.toResponse(po, getDetailsUseCase.execute(po.getId()));
    }
}
