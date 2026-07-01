package org.lvtn.mws.interfaces.rest;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.application.usecases.picking.AssignPickingListUseCase;
import org.lvtn.mws.application.usecases.picking.CompletePickingListUseCase;
import org.lvtn.mws.application.usecases.picking.ConfirmPickUseCase;
import org.lvtn.mws.application.usecases.picking.CreatePickingListUseCase;
import org.lvtn.mws.application.usecases.picking.GetAllPickingListsUseCase;
import org.lvtn.mws.application.usecases.picking.GetPickingListByIdUseCase;
import org.lvtn.mws.application.usecases.picking.ReportShortPickUseCase;
import org.lvtn.mws.interfaces.dto.request.picking.AssignPickingRequest;
import org.lvtn.mws.interfaces.dto.request.picking.ConfirmPickRequest;
import org.lvtn.mws.interfaces.dto.request.picking.CreatePickingListRequest;
import org.lvtn.mws.interfaces.dto.request.picking.ShortPickRequest;
import org.lvtn.mws.interfaces.dto.response.picking.PickingListResponse;
import org.lvtn.mws.interfaces.mapper.PickingWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/v1/picking-lists")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('OUTBOUND_VIEW')")
public class PickingListController {

    private final CreatePickingListUseCase createPickingListUseCase;
    private final AssignPickingListUseCase assignPickingListUseCase;
    private final ConfirmPickUseCase confirmPickUseCase;
    private final ReportShortPickUseCase reportShortPickUseCase;
    private final CompletePickingListUseCase completePickingListUseCase;
    private final GetPickingListByIdUseCase getPickingListByIdUseCase;
    private final GetAllPickingListsUseCase getAllPickingListsUseCase;
    private final PickingWebMapper mapper;

    /** Sinh lệnh gom hàng theo FEFO cho đơn đã ALLOCATED; chuyển SO -> PICKING. */
    @PreAuthorize("hasAuthority('OUTBOUND_PICK')")
    @PostMapping
    public ResponseEntity<PickingListResponse> create(@Valid @RequestBody CreatePickingListRequest req) {
        var pl = createPickingListUseCase.execute(req.soId());
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(pl));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PickingListResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(mapper.toResponse(getPickingListByIdUseCase.execute(id)));
    }

    @GetMapping
    public ResponseEntity<List<PickingListResponse>> getAll() {
        return ResponseEntity.ok(mapper.toResponseList(getAllPickingListsUseCase.execute()));
    }

    /** Giao việc cho nhân viên kho (PENDING -> PICKING). */
    @PreAuthorize("hasAuthority('OUTBOUND_PICK')")
    @PostMapping("/{id}/assign")
    public ResponseEntity<PickingListResponse> assign(@PathVariable String id,
                                                      @Valid @RequestBody AssignPickingRequest req) {
        return ResponseEntity.ok(mapper.toResponse(
                assignPickingListUseCase.execute(id, req.userId())));
    }

    /** Đối soát barcode cho 1 dòng gom hàng (so actual_batch_id với batch_id hệ thống gán). */
    @PreAuthorize("hasAuthority('OUTBOUND_PICK')")
    @PostMapping("/details/{detailId}/confirm")
    public ResponseEntity<PickingListResponse> confirm(@PathVariable String detailId,
                                                       @Valid @RequestBody ConfirmPickRequest req) {
        return ResponseEntity.ok(mapper.toResponse(confirmPickUseCase.execute(
                detailId, req.scannedBatchNumber(), req.confirmedBy())));
    }

    /** Báo thiếu hàng thực tế cho 1 dòng; hệ thống tự bù phần thiếu từ lô FEFO kế tiếp. */
    @PreAuthorize("hasAuthority('OUTBOUND_PICK')")
    @PostMapping("/details/{detailId}/short")
    public ResponseEntity<PickingListResponse> reportShort(@PathVariable String detailId,
                                                           @Valid @RequestBody ShortPickRequest req) {
        return ResponseEntity.ok(mapper.toResponse(reportShortPickUseCase.execute(
                detailId, req.scannedBatchNumber(), req.actualQty(), req.reason(), req.confirmedBy())));
    }

    /** Hoàn tất gom hàng khi mọi dòng đã xác nhận (PICKING -> COMPLETED). */
    @PreAuthorize("hasAuthority('OUTBOUND_PICK')")
    @PostMapping("/{id}/complete")
    public ResponseEntity<PickingListResponse> complete(@PathVariable String id) {
        return ResponseEntity.ok(mapper.toResponse(completePickingListUseCase.execute(id)));
    }
}
