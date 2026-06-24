package org.lvtn.mws.interfaces.rest;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.application.usecases.stockmovement.GetStockMovementsUseCase;
import org.lvtn.mws.interfaces.dto.response.StockMovementResponse;
import org.lvtn.mws.interfaces.mapper.StockMovementWebMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Truy vết thẻ kho (Audit Trail) — chỉ đọc.
 * [GIAI ĐOẠN 7] Yêu cầu quyền AUDIT_VIEW_MOVEMENTS (ADMIN, WH_MANAGER).
 * Hỗ trợ tra theo: sản phẩm; sản phẩm + kho; hoặc chứng từ gốc (reference).
 */
@RestController
@RequestMapping("/api/stock-movements")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('AUDIT_VIEW_MOVEMENTS')")
public class StockMovementController {

    private final GetStockMovementsUseCase getStockMovementsUseCase;
    private final StockMovementWebMapper mapper;

    /** Tra theo sản phẩm; kèm warehouseId (tuỳ chọn) để giới hạn trong 1 kho. */
    @GetMapping(params = "productId")
    public ResponseEntity<List<StockMovementResponse>> byProduct(
            @RequestParam String productId,
            @RequestParam(required = false) String warehouseId) {
        List<StockMovementResponse> body = (warehouseId == null || warehouseId.isBlank())
                ? mapper.toResponseList(getStockMovementsUseCase.byProduct(productId))
                : mapper.toResponseList(getStockMovementsUseCase.byProductAndWarehouse(productId, warehouseId));
        return ResponseEntity.ok(body);
    }

    /** Tra ngược theo chứng từ gốc (GOODS_RECEIPT / SALES_ORDER / TRANSFER_ORDER ...). */
    @GetMapping(params = {"referenceType", "referenceId"})
    public ResponseEntity<List<StockMovementResponse>> byReference(
            @RequestParam String referenceType,
            @RequestParam String referenceId) {
        return ResponseEntity.ok(mapper.toResponseList(
                getStockMovementsUseCase.byReference(referenceType, referenceId)));
    }
}
