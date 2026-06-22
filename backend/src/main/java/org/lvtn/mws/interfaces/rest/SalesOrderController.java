package org.lvtn.mws.interfaces.rest;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.application.usecases.salesorder.AllocateSalesOrderUseCase;
import org.lvtn.mws.application.usecases.salesorder.CancelSalesOrderUseCase;
import org.lvtn.mws.application.usecases.salesorder.CreateSalesOrderUseCase;
import org.lvtn.mws.application.usecases.salesorder.GetAllSalesOrdersUseCase;
import org.lvtn.mws.application.usecases.salesorder.GetSalesOrderByIdUseCase;
import org.lvtn.mws.domain.model.SalesOrderLineCommand;
import org.lvtn.mws.interfaces.dto.request.salesorder.CreateSalesOrderRequest;
import org.lvtn.mws.interfaces.dto.response.salesorder.SalesOrderResponse;
import org.lvtn.mws.interfaces.mapper.SalesOrderWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sales-orders")
@RequiredArgsConstructor
public class SalesOrderController {

    private final CreateSalesOrderUseCase createSalesOrderUseCase;
    private final GetSalesOrderByIdUseCase getSalesOrderByIdUseCase;
    private final GetAllSalesOrdersUseCase getAllSalesOrdersUseCase;
    private final AllocateSalesOrderUseCase allocateSalesOrderUseCase;
    private final CancelSalesOrderUseCase cancelSalesOrderUseCase;
    private final SalesOrderWebMapper mapper;

    @PostMapping
    public ResponseEntity<SalesOrderResponse> create(@Valid @RequestBody CreateSalesOrderRequest req) {
        List<SalesOrderLineCommand> lines = req.lines().stream()
                .map(l -> new SalesOrderLineCommand(
                        l.productId(), l.quantityOrdered(), l.unitPrice(), l.discountPercent()))
                .toList();
        var so = createSalesOrderUseCase.execute(
                req.warehouseId(), req.customerId(), req.discountAmount(),
                req.priority(), req.requiredDate(), req.createdBy(), lines);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(so));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SalesOrderResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(mapper.toResponse(getSalesOrderByIdUseCase.execute(id)));
    }

    @GetMapping
    public ResponseEntity<List<SalesOrderResponse>> getAll() {
        return ResponseEntity.ok(mapper.toResponseList(getAllSalesOrdersUseCase.execute()));
    }

    /** DRAFT -> ALLOCATED: giữ chỗ tồn kho cho từng dòng (rollback nếu thiếu). */
    @PostMapping("/{id}/allocate")
    public ResponseEntity<SalesOrderResponse> allocate(@PathVariable String id) {
        return ResponseEntity.ok(mapper.toResponse(allocateSalesOrderUseCase.execute(id)));
    }

    /** Hủy đơn: giải phóng tồn kho đã giữ chỗ. */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<SalesOrderResponse> cancel(@PathVariable String id) {
        return ResponseEntity.ok(mapper.toResponse(cancelSalesOrderUseCase.execute(id)));
    }
}
