package org.lvtn.mws.interfaces.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.lvtn.mws.application.usecases.supplier.*;
import org.lvtn.mws.domain.model.Supplier;
import org.lvtn.mws.interfaces.dto.request.supplier.CreateSupplierRequest;
import org.lvtn.mws.interfaces.dto.request.supplier.UpdateSupplierRequest;
import org.lvtn.mws.interfaces.dto.response.supplier.SupplierResponse;
import org.lvtn.mws.interfaces.mapper.SupplierWebMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final CreateSupplierUseCase createUseCase;
    private final GetAllSuppliersUseCase getAllUseCase;
    private final GetSupplierByIdUseCase getByIdUseCase;
    private final UpdateSupplierUseCase updateUseCase;
    private final DeleteSupplierUseCase deleteUseCase;
    private final SupplierWebMapper webMapper;

    @PostMapping
    public ResponseEntity<SupplierResponse> create(@Valid @RequestBody CreateSupplierRequest req) {
        Supplier s = createUseCase.execute(req.getCode(), req.getName(), req.getContactName(),
                req.getPhone(), req.getEmail(), req.getAddress());
        return ResponseEntity.status(HttpStatus.CREATED).body(webMapper.toResponse(s));
    }

    @GetMapping
    public ResponseEntity<List<SupplierResponse>> getAll() {
        return ResponseEntity.ok(webMapper.toResponseList(getAllUseCase.execute()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SupplierResponse> getById(@PathVariable("id") String id) {
        return ResponseEntity.ok(webMapper.toResponse(getByIdUseCase.execute(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SupplierResponse> update(@PathVariable("id") String id,
                                                   @Valid @RequestBody UpdateSupplierRequest req) {
        Supplier s = updateUseCase.execute(id, req.getName(), req.getContactName(),
                req.getPhone(), req.getEmail(), req.getAddress());
        return ResponseEntity.ok(webMapper.toResponse(s));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") String id) {
        deleteUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }
}
