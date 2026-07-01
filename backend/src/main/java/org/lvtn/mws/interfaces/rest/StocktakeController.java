package org.lvtn.mws.interfaces.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.lvtn.mws.application.usecases.stocktake.ApproveStocktakeLineUseCase;
import org.lvtn.mws.application.usecases.stocktake.CompleteStocktakeSessionUseCase;
import org.lvtn.mws.application.usecases.stocktake.GetAllStocktakesUseCase;
import org.lvtn.mws.application.usecases.stocktake.GetStocktakeByIdUseCase;
import org.lvtn.mws.application.usecases.stocktake.GetStocktakeDetailsUseCase;
import org.lvtn.mws.application.usecases.stocktake.StartStocktakeUseCase;
import org.lvtn.mws.application.usecases.stocktake.SubmitCountedQuantityUseCase;
import org.lvtn.mws.domain.model.StocktakeSession;
import org.lvtn.mws.infrastructure.security.UserPrincipal;
import org.lvtn.mws.interfaces.dto.request.stocktake.ApproveLineRequest;
import org.lvtn.mws.interfaces.dto.request.stocktake.StartStocktakeRequest;
import org.lvtn.mws.interfaces.dto.request.stocktake.SubmitCountRequest;
import org.lvtn.mws.interfaces.dto.response.adjustment.AdjustmentVoucherResponse;
import org.lvtn.mws.interfaces.dto.response.stocktake.CompleteStocktakeResponse;
import org.lvtn.mws.interfaces.dto.response.stocktake.StocktakeResponse;
import org.lvtn.mws.interfaces.dto.response.stocktake.StocktakeSessionResponse;
import org.lvtn.mws.interfaces.mapper.AdjustmentWebMapper;
import org.lvtn.mws.interfaces.mapper.StocktakeWebMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * API Kiểm kê kho (Giai đoạn 6). Bảo mật: chỉ cần authenticated() như module Inventory
 * (không gắn @PreAuthorize). Phân quyền duyệt theo ngưỡng % được kiểm ở bước duyệt phiếu điều chỉnh.
 */
@RestController
@RequestMapping("/api/v1/stocktakes")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('STOCKTAKE_VIEW')")
public class StocktakeController {

    private final StartStocktakeUseCase startUseCase;
    private final SubmitCountedQuantityUseCase submitUseCase;
    private final ApproveStocktakeLineUseCase approveLineUseCase;
    private final CompleteStocktakeSessionUseCase completeUseCase;
    private final GetStocktakeByIdUseCase getByIdUseCase;
    private final GetStocktakeDetailsUseCase getDetailsUseCase;
    private final GetAllStocktakesUseCase getAllUseCase;
    private final StocktakeWebMapper webMapper;
    private final AdjustmentWebMapper adjustmentWebMapper;

    @PreAuthorize("hasAuthority('STOCKTAKE_MANAGE')")
    @PostMapping
    public ResponseEntity<StocktakeResponse> start(@Valid @RequestBody StartStocktakeRequest request,
                                                   Authentication authentication) {
        StocktakeSession session = startUseCase.execute(request.warehouseId(), currentUserId(authentication));
        return ResponseEntity.status(HttpStatus.CREATED).body(buildResponse(session));
    }

    @GetMapping
    public ResponseEntity<List<StocktakeSessionResponse>> list() {
        return ResponseEntity.ok(getAllUseCase.execute().stream()
                .map(webMapper::toSessionResponse).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<StocktakeResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(buildResponse(getByIdUseCase.execute(id)));
    }

    @PreAuthorize("hasAuthority('STOCKTAKE_MANAGE')")
    @PostMapping("/details/{detailId}/count")
    public ResponseEntity<?> count(@PathVariable String detailId,
                                   @Valid @RequestBody SubmitCountRequest request,
                                   Authentication authentication) {
        return ResponseEntity.ok(webMapper.toDetailResponse(
                submitUseCase.execute(detailId, request.countedQuantity(), currentUserId(authentication))));
    }

    @PreAuthorize("hasAuthority('STOCKTAKE_APPROVE')")
    @PostMapping("/details/{detailId}/approve-line")
    public ResponseEntity<?> approveLine(@PathVariable String detailId,
                                         @RequestBody(required = false) ApproveLineRequest request,
                                         Authentication authentication) {
        String reason = request == null ? null : request.reason();
        return ResponseEntity.ok(webMapper.toDetailResponse(
                approveLineUseCase.execute(detailId, currentUserId(authentication), reason)));
    }

    @PreAuthorize("hasAuthority('STOCKTAKE_MANAGE')")
    @PostMapping("/{id}/complete")
    public ResponseEntity<CompleteStocktakeResponse> complete(@PathVariable String id,
                                                              Authentication authentication) {
        CompleteStocktakeSessionUseCase.Result result =
                completeUseCase.execute(id, currentUserId(authentication));
        AdjustmentVoucherResponse voucher = result.voucher() == null
                ? null : adjustmentWebMapper.toResponse(result.voucher());
        CompleteStocktakeResponse body = new CompleteStocktakeResponse(
                webMapper.toSessionResponse(result.session()),
                webMapper.toDetailResponseList(getDetailsUseCase.execute(id)),
                voucher);
        return ResponseEntity.ok(body);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private StocktakeResponse buildResponse(StocktakeSession session) {
        return webMapper.toResponse(session, getDetailsUseCase.execute(session.getId()));
    }

    /** Lấy userId (FK users.id) từ principal; fallback về username nếu không phải UserPrincipal. */
    private String currentUserId(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserPrincipal up) {
            return up.getId();
        }
        return authentication.getName();
    }
}
