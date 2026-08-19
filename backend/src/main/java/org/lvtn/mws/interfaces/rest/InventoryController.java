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
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('INVENTORY_VIEW')")
public class InventoryController {

    private final InitInventoryUseCase initUseCase;
    private final GetInventoryUseCase getUseCase;
    private final ReserveStockUseCase reserveUseCase;
    private final ReleaseStockUseCase releaseUseCase;
    private final AllocateBatchesUseCase allocateUseCase;
    private final CommitStockDeductionUseCase commitUseCase;
    private final CreateInventoryBatchUseCase createBatchUseCase;
    private final GetBatchesUseCase getBatchesUseCase;
    private final GetBatchSuppliersUseCase getBatchSuppliersUseCase;
    private final org.lvtn.mws.application.usecases.inventory.GetAvailableBySupplierUseCase getAvailableBySupplierUseCase;
    private final org.lvtn.mws.application.usecases.inventory.GetSellableByWarehouseUseCase getSellableByWarehouseUseCase;
    private final GetExpiringBatchesUseCase getExpiringBatchesUseCase;
    private final org.lvtn.mws.application.usecases.warehouse.GetBinLocationCodesUseCase getBinLocationCodesUseCase;
    private final UpdateBatchStatusUseCase updateBatchStatusUseCase;
    private final org.lvtn.mws.domain.repository.ISupplierRepository supplierRepository;
    private final InventoryWebMapper mapper;

    // ── Inventory aggregate ───────────────────────────────────────────────

    @PreAuthorize("hasAuthority('INVENTORY_ADJUST')")
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
        Map<String, String> productSuppliers = getBatchSuppliersUseCase.productSuppliersByWarehouse(warehouseId);
        List<InventoryResponse> list = mapper.toResponseList(getUseCase.executeByWarehouse(warehouseId));
        for (InventoryResponse r : list) {
            r.setSupplierName(productSuppliers.get(r.getProductId()));
        }
        return list;
    }

    /** [Bán theo NCC] Tồn khả dụng gom theo NCC cho (sản phẩm, kho) — cho dropdown chọn NCC ở đơn bán. */
    @GetMapping("/available-by-supplier")
    public List<org.lvtn.mws.interfaces.dto.response.inventory.AvailableBySupplierResponse> availableBySupplier(
            @RequestParam String productId,
            @RequestParam String warehouseId) {
        return getAvailableBySupplierUseCase.execute(productId, warehouseId);
    }

    /** [Bán theo NCC] Sản phẩm CÓ THỂ BÁN trong kho (chỉ lô ACTIVE & chưa hết hạn) — cho dropdown chọn SP. */
    @GetMapping("/sellable-by-warehouse")
    public List<org.lvtn.mws.interfaces.dto.response.inventory.SellableProductResponse> sellableByWarehouse(
            @RequestParam String warehouseId) {
        return getSellableByWarehouseUseCase.execute(warehouseId);
    }

    // ── Stock operations ──────────────────────────────────────────────────

    @PreAuthorize("hasAuthority('INVENTORY_ADJUST')")
    @PostMapping("/reserve")
    public InventoryResponse reserve(@Valid @RequestBody ReserveStockRequest req) {
        return mapper.toResponse(
                reserveUseCase.execute(req.getProductId(), req.getWarehouseId(), req.getQuantity()));
    }

    @PreAuthorize("hasAuthority('INVENTORY_ADJUST')")
    @PostMapping("/release")
    public InventoryResponse release(@Valid @RequestBody ReleaseStockRequest req) {
        return mapper.toResponse(
                releaseUseCase.execute(req.getProductId(), req.getWarehouseId(), req.getQuantity()));
    }

    @PreAuthorize("hasAuthority('INVENTORY_ADJUST')")
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

    @PreAuthorize("hasAuthority('INVENTORY_ADJUST')")
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
        Map<String, String> binCodes = getBinLocationCodesUseCase.executeByWarehouse(warehouseId);
        // Nguồn NCC ưu tiên: cột supplier_id đóng trên chính bản ghi inventory_batches
        // (từ khi có tính năng "Bán theo NCC" — xem GoodsReceiptDomainService.upsertBatch).
        // Fallback (cho lô cũ hoặc lô tạo qua adjust/transfer chưa gắn supplier_id):
        // truy ngược qua goods_receipt_details theo batchNumber. Nếu cả 2 đều null → "—" ở FE.
        Map<String, String> supplierNamesById = supplierRepository.findAll().stream()
                .collect(java.util.stream.Collectors.toMap(
                        org.lvtn.mws.domain.model.Supplier::getId,
                        org.lvtn.mws.domain.model.Supplier::getName,
                        (a, b) -> a));
        Map<String, String> batchSuppliersLegacy = getBatchSuppliersUseCase.executeByProductAndWarehouse(productId, warehouseId);
        return getBatchesUseCase.execute(productId, warehouseId).stream()
                .map(b -> {
                    String name = b.getSupplierId() != null
                            ? supplierNamesById.get(b.getSupplierId())
                            : batchSuppliersLegacy.get(b.getBatchNumber());
                    return mapper.toBatchResponse(b, binCodes.get(b.getBinLocationId()), name);
                })
                .toList();
    }

    /** [MỤC 6] Lô còn hàng có hạn dùng ≤ hôm nay + days (kho tuỳ chọn). */
    @GetMapping("/batches/expiring")
    public List<InventoryBatchResponse> getExpiringBatches(
            @RequestParam(defaultValue = "30") int days,
            @RequestParam(required = false) String warehouseId) {
        return mapper.toBatchResponseList(getExpiringBatchesUseCase.execute(days, warehouseId));
    }

    @PreAuthorize("hasAuthority('INVENTORY_ADJUST')")
    @PatchMapping("/batches/{batchId}/status")
    public InventoryBatchResponse updateBatchStatus(@PathVariable String batchId,
                                                    @Valid @RequestBody UpdateBatchStatusRequest req) {
        return mapper.toBatchResponse(updateBatchStatusUseCase.execute(batchId, req.getStatus()));
    }
}
