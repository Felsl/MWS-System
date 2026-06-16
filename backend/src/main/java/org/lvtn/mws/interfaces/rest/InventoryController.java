package org.lvtn.mws.interfaces.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.lvtn.mws.application.usecases.inventory.*;
import org.lvtn.mws.domain.model.BatchDeductionRequest;
import org.lvtn.mws.interfaces.dto.request.inventory.*;
import org.lvtn.mws.interfaces.dto.response.inventory.*;
import org.lvtn.mws.interfaces.mapper.InventoryWebMapper;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InitInventoryUseCase initUseCase;
    private final GetInventoryUseCase getUseCase;
    private final ReserveStockUseCase reserveUseCase;
    private final ReleaseStockUseCase releaseUseCase;
    private final AllocateBatchesUseCase allocateUseCase;
    private final CommitStockDeductionUseCase commitUseCase;
    private final CreateInventoryBatchUseCase createBatchUseCase;
    private final GetBatchesUseCase getBatchesUseCase;
    private final UpdateBatchStatusUseCase updateBatchStatusUseCase;
    private final InventoryWebMapper mapper;

    // ── Inventory aggregate ───────────────────────────────────────────────

    @PostMapping("/init")
    @ResponseStatus(HttpStatus.CREATED)
    public InventoryResponse init(@RequestParam String productId,
                                  @RequestParam String warehouseId) {
        return mapper.toResponse(initUseCase.execute(productId, warehouseId));
    }

    @GetMapping
    public InventoryResponse get(@RequestParam String productId,
                                 @RequestParam String warehouseId) {
        return mapper.toResponse(getUseCase.executeByProductAndWarehouse(productId, warehouseId));
    }

    @GetMapping("/warehouse/{warehouseId}")
    public List<InventoryResponse> getByWarehouse(@PathVariable String warehouseId) {
        return mapper.toResponseList(getUseCase.executeByWarehouse(warehouseId));
    }

    // ── Stock operations ──────────────────────────────────────────────────

    @PostMapping("/reserve")
    public InventoryResponse reserve(@Valid @RequestBody ReserveStockRequest req) {
        return mapper.toResponse(
                reserveUseCase.execute(req.getProductId(), req.getWarehouseId(), req.getQuantity()));
    }

    @PostMapping("/release")
    public InventoryResponse release(@Valid @RequestBody ReleaseStockRequest req) {
        return mapper.toResponse(
                releaseUseCase.execute(req.getProductId(), req.getWarehouseId(), req.getQuantity()));
    }

    @PostMapping("/commit")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void commit(@Valid @RequestBody CommitStockRequest req) {
        List<BatchDeductionRequest> details = req.getDetails().stream()
                .map(d -> new BatchDeductionRequest(d.getBatchId(), d.getQuantity()))
                .toList();
        commitUseCase.execute(req.getProductId(), req.getWarehouseId(), details);
    }

    // ── FEFO Allocation ───────────────────────────────────────────────────

    @GetMapping("/batches/allocate")
    public List<BatchSuggestionResponse> allocate(@RequestParam String productId,
                                                  @RequestParam String warehouseId,
                                                  @RequestParam int quantity) {
        return mapper.toSuggestionResponseList(
                allocateUseCase.execute(productId, warehouseId, quantity));
    }

    // ── Batch CRUD ────────────────────────────────────────────────────────

    @PostMapping("/batches")
    @ResponseStatus(HttpStatus.CREATED)
    public InventoryBatchResponse createBatch(@Valid @RequestBody CreateBatchRequest req) {
        return mapper.toBatchResponse(createBatchUseCase.execute(
                req.getProductId(), req.getWarehouseId(), req.getBinLocationId(),
                req.getQuantity(), req.getExpiryDate(), req.getManufacturedDate()));
    }

    @GetMapping("/batches")
    public List<InventoryBatchResponse> getBatches(@RequestParam String productId,
                                                   @RequestParam String warehouseId) {
        return mapper.toBatchResponseList(getBatchesUseCase.execute(productId, warehouseId));
    }

    @PatchMapping("/batches/{batchId}/status")
    public InventoryBatchResponse updateBatchStatus(@PathVariable String batchId,
                                                    @Valid @RequestBody UpdateBatchStatusRequest req) {
        return mapper.toBatchResponse(updateBatchStatusUseCase.execute(batchId, req.getStatus()));
    }
}
