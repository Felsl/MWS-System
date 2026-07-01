package org.lvtn.mws.interfaces.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.lvtn.mws.application.usecases.customer.*;
import org.lvtn.mws.domain.model.Customer;
import org.lvtn.mws.interfaces.dto.request.customer.CreateCustomerRequest;
import org.lvtn.mws.interfaces.dto.request.customer.UpdateCustomerRequest;
import org.lvtn.mws.interfaces.dto.response.customer.CustomerResponse;
import org.lvtn.mws.interfaces.mapper.CustomerWebMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('MASTER_PARTNER_MANAGE')")
public class CustomerController {

    private final CreateCustomerUseCase createUseCase;
    private final GetAllCustomersUseCase getAllUseCase;
    private final GetCustomerByIdUseCase getByIdUseCase;
    private final UpdateCustomerUseCase updateUseCase;
    private final DeleteCustomerUseCase deleteUseCase;
    private final CustomerWebMapper webMapper;

    @PostMapping
    public ResponseEntity<CustomerResponse> create(@Valid @RequestBody CreateCustomerRequest req) {
        Customer c = createUseCase.execute(req.getCode(), req.getName(), req.getTaxCode(),
                req.getPhone(), req.getEmail(), req.getAddress());
        return ResponseEntity.status(HttpStatus.CREATED).body(webMapper.toResponse(c));
    }

    @GetMapping
    public ResponseEntity<List<CustomerResponse>> getAll() {
        return ResponseEntity.ok(webMapper.toResponseList(getAllUseCase.execute()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> getById(@PathVariable("id") String id) {
        return ResponseEntity.ok(webMapper.toResponse(getByIdUseCase.execute(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponse> update(@PathVariable("id") String id,
                                                   @Valid @RequestBody UpdateCustomerRequest req) {
        Customer c = updateUseCase.execute(id, req.getName(), req.getTaxCode(),
                req.getPhone(), req.getEmail(), req.getAddress());
        return ResponseEntity.ok(webMapper.toResponse(c));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") String id) {
        deleteUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }
}
