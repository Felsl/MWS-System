package org.lvtn.mws.interfaces.rest;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.application.usecases.goodsreceipt.CompleteGoodsReceiptUseCase;
import org.lvtn.mws.application.usecases.goodsreceipt.CreateGoodsReceiptUseCase;
import org.lvtn.mws.application.usecases.goodsreceipt.GetGoodsReceiptByIdUseCase;
import org.lvtn.mws.application.usecases.goodsreceipt.GetGoodsReceiptDetailsUseCase;
import org.lvtn.mws.domain.model.GoodsReceipt;
import org.lvtn.mws.interfaces.dto.request.goodsreceipt.CreateGoodsReceiptRequest;
import org.lvtn.mws.interfaces.dto.response.goodsreceipt.GoodsReceiptResponse;
import org.lvtn.mws.interfaces.mapper.GoodsReceiptWebMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/v1/goods-receipts")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('INBOUND_VIEW_GRN')")
public class GoodsReceiptController {

    private final CreateGoodsReceiptUseCase createUseCase;
    private final CompleteGoodsReceiptUseCase completeUseCase;
    private final GetGoodsReceiptByIdUseCase getByIdUseCase;
    private final GetGoodsReceiptDetailsUseCase getDetailsUseCase;
    private final GoodsReceiptWebMapper webMapper;

    @PreAuthorize("hasAuthority('INBOUND_CREATE_GRN')")
    @PostMapping
    public ResponseEntity<GoodsReceiptResponse> create(
            @Valid @RequestBody CreateGoodsReceiptRequest request,
            Authentication authentication) {
        GoodsReceipt grn = createUseCase.execute(
                request.getPoId(),
                request.getWarehouseId(),
                authentication.getName(),
                request.getNote(),
                webMapper.toCommands(request.getLines()));
        return ResponseEntity.status(HttpStatus.CREATED).body(buildResponse(grn));
    }

    @PreAuthorize("hasAuthority('INBOUND_COMPLETE_GRN')")
    @PostMapping("/{id}/complete")
    public ResponseEntity<GoodsReceiptResponse> complete(@PathVariable String id) {
        return ResponseEntity.ok(buildResponse(completeUseCase.execute(id)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GoodsReceiptResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(buildResponse(getByIdUseCase.execute(id)));
    }

    /** Assembles the full response (header + detail lines) for a goods receipt. */
    private GoodsReceiptResponse buildResponse(GoodsReceipt grn) {
        return webMapper.toResponse(grn, getDetailsUseCase.execute(grn.getId()));
    }
}
