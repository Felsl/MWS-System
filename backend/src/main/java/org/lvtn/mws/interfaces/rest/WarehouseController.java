package org.lvtn.mws.interfaces.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.lvtn.mws.application.usecases.warehouse.*;
import org.lvtn.mws.interfaces.dto.request.warehouse.BulkGenerateBinRequest;
import org.lvtn.mws.interfaces.dto.request.warehouse.CreateWarehouseRequest;
import org.lvtn.mws.interfaces.dto.request.warehouse.UpdateWarehouseRequest;
import org.lvtn.mws.interfaces.dto.response.warehouse.BinLocationResponse;
import org.lvtn.mws.interfaces.dto.response.warehouse.BulkGenerateResultResponse;
import org.lvtn.mws.interfaces.dto.response.warehouse.WarehouseResponse;
import org.lvtn.mws.interfaces.mapper.WarehouseWebMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/warehouses")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('WAREHOUSE_VIEW')")
public class WarehouseController {

    private final CreateWarehouseUseCase           createUseCase;
    private final GetAllWarehousesUseCase          getAllUseCase;
    private final GetAllWarehousesAdminUseCase     getAllAdminUseCase;
    private final GetWarehouseByIdUseCase          getByIdUseCase;
    private final UpdateWarehouseUseCase           updateUseCase;
    private final DeleteWarehouseUseCase           deleteUseCase;
    private final BulkGenerateBinLocationsUseCase  bulkGenerateUseCase;
    private final GetBinLocationsByWarehouseUseCase getBinLocationsUseCase;
    private final DeleteBinLocationUseCase         deleteBinLocationUseCase;
    private final WarehouseWebMapper               mapper;

    // ─── Warehouse CRUD ────────────────────────────────────────────────────

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('WAREHOUSE_CREATE')")
    public WarehouseResponse create(@Valid @RequestBody CreateWarehouseRequest request) {
        return mapper.toResponse(
                createUseCase.execute(request.getCode(), request.getName(), request.getAddress()));
    }

    /**
     * GET /api/v1/warehouses
     * - STOREKEEPER, PICKER: chỉ thấy kho được giao (WarehouseScoped AOP tự filter)
     * - ADMIN: thấy tất cả kho
     */
    @GetMapping
    public List<WarehouseResponse> getAll(
            @RequestParam(defaultValue = "false") boolean adminView) {
        if (adminView) {
            return getAllAdminUseCase.execute().stream()
                    .map(mapper::toResponse).collect(Collectors.toList());
        }
        return getAllUseCase.execute().stream()
                .map(mapper::toResponse).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public WarehouseResponse getById(@PathVariable String id) {
        return mapper.toResponse(getByIdUseCase.execute(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('WAREHOUSE_UPDATE')")
    public WarehouseResponse update(@PathVariable String id,
                                    @Valid @RequestBody UpdateWarehouseRequest request) {
        return mapper.toResponse(
                updateUseCase.execute(id, request.getCode(), request.getName(), request.getAddress()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('WAREHOUSE_DELETE')")
    public void delete(@PathVariable String id) {
        deleteUseCase.execute(id);
    }

    // ─── BinLocation ───────────────────────────────────────────────────────

    /**
     * POST /api/v1/warehouses/{id}/bin-locations/bulk
     *
     * Body ví dụ:
     * {
     *   "zones": [
     *     {
     *       "zone": "A",
     *       "aisles": [
     *         {
     *           "aisle": "01",
     *           "racks": [
     *             { "rack": "R1", "bins": ["B1","B2","B3","B4","B5"] },
     *             { "rack": "R2", "bins": ["B1","B2","B3","B4","B5"] }
     *           ]
     *         }
     *       ]
     *     }
     *   ]
     * }
     *
     * Kết quả: tự động tạo 10 ô kệ A-01-R1-B1 đến A-01-R2-B5
     */
    @PostMapping("/{id}/bin-locations/bulk")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('WAREHOUSE_CREATE')")
    public BulkGenerateResultResponse bulkGenerateBinLocations(
            @PathVariable String id,
            @Valid @RequestBody BulkGenerateBinRequest request) {

        List<BinLocationResponse> generated = bulkGenerateUseCase
                .execute(id, request.getZones())
                .stream()
                .map(mapper::toBinResponse)
                .collect(Collectors.toList());

        return BulkGenerateResultResponse.builder()
                .totalGenerated(generated.size())
                .binLocations(generated)
                .build();
    }

    @GetMapping("/{id}/bin-locations")
    public List<BinLocationResponse> getBinLocations(@PathVariable String id) {
        return getBinLocationsUseCase.execute(id)
                .stream().map(mapper::toBinResponse).collect(Collectors.toList());
    }

    @DeleteMapping("/{warehouseId}/bin-locations/{binId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('WAREHOUSE_DELETE')")
    public void deleteBinLocation(@PathVariable String warehouseId,
                                   @PathVariable String binId) {
        deleteBinLocationUseCase.execute(binId);
    }
}
