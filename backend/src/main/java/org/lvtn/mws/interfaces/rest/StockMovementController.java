package org.lvtn.mws.interfaces.rest;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.application.usecases.stockmovement.GetStockMovementsUseCase;
import org.lvtn.mws.interfaces.dto.response.StockMovementResponse;
import org.lvtn.mws.interfaces.mapper.StockMovementWebMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Truy vết thẻ kho (Audit Trail) — chỉ đọc. */
@RestController
@RequestMapping("/api/stock-movements")
@RequiredArgsConstructor
public class StockMovementController {

    private final GetStockMovementsUseCase getStockMovementsUseCase;
    private final StockMovementWebMapper mapper;

    @GetMapping(params = "productId")
    public ResponseEntity<List<StockMovementResponse>> byProduct(@RequestParam String productId) {
        return ResponseEntity.ok(mapper.toResponseList(
                getStockMovementsUseCase.byProduct(productId)));
    }

    @GetMapping(params = {"referenceType", "referenceId"})
    public ResponseEntity<List<StockMovementResponse>> byReference(
            @RequestParam String referenceType,
            @RequestParam String referenceId) {
        return ResponseEntity.ok(mapper.toResponseList(
                getStockMovementsUseCase.byReference(referenceType, referenceId)));
    }
}
