package org.lvtn.mws.interfaces.rest;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.application.usecases.transfer.GenerateTransferPickingUseCase;
import org.lvtn.mws.application.usecases.transfer.ConfirmTransferPickUseCase;
import org.lvtn.mws.application.usecases.transfer.GetTransferPickingUseCase;
import jakarta.validation.Valid;
import org.lvtn.mws.interfaces.dto.request.picking.ConfirmPickRequest;
import org.lvtn.mws.interfaces.dto.response.picking.PickingListResponse;
import org.lvtn.mws.interfaces.mapper.PickingWebMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Lệnh gom hàng (picking) cho ĐIỀU CHUYỂN — tách riêng khỏi picking của đơn xuất.
 * Luồng: (đã APPROVED) -> POST /picking (sinh lệnh, phiếu sang PICKING) -> picker quét (Increment 2b).
 */
@RestController
@RequestMapping("/api/v1/transfer-orders")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('TRANSFER_VIEW')")
public class TransferPickingController {

    private final GenerateTransferPickingUseCase generateUseCase;
    private final GetTransferPickingUseCase getUseCase;
    private final ConfirmTransferPickUseCase confirmUseCase;
    private final PickingWebMapper pickingMapper;

    /** Sinh lệnh gom hàng cho phiếu điều chuyển đã duyệt. */
    @PreAuthorize("hasAuthority('TRANSFER_DISPATCH')")
    @PostMapping("/{transferId}/picking")
    public ResponseEntity<PickingListResponse> generate(@PathVariable String transferId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(pickingMapper.toResponse(generateUseCase.execute(transferId)));
    }

    /** Xem lệnh gom hàng của phiếu điều chuyển. */
    @GetMapping("/{transferId}/picking")
    public ResponseEntity<PickingListResponse> get(@PathVariable String transferId) {
        return ResponseEntity.ok(pickingMapper.toResponse(getUseCase.execute(transferId)));
    }

    /**
     * Picker quét lô cho 1 dòng gom hàng điều chuyển. Dòng khoá-lô phải quét đúng lô chỉ định;
     * dòng gợi ý FEFO nhận bất kỳ lô ACTIVE còn tồn hợp lệ tại kho nguồn.
     */
    @PreAuthorize("hasAuthority('TRANSFER_DISPATCH')")
    @PostMapping("/picking/details/{detailId}/scan")
    public ResponseEntity<PickingListResponse> scan(@PathVariable String detailId,
                                                    @Valid @RequestBody ConfirmPickRequest req) {
        return ResponseEntity.ok(pickingMapper.toResponse(
                confirmUseCase.execute(detailId, req.scannedBatchNumber(), req.confirmedBy())));
    }
}
