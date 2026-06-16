package org.lvtn.mws.interfaces.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.lvtn.mws.application.usecases.user.AssignWarehouseAccessUseCase;
import org.lvtn.mws.application.usecases.user.GetUserWarehouseAccessUseCase;
import org.lvtn.mws.interfaces.dto.request.warehouse.AssignWarehouseRequestDTO;
import org.lvtn.mws.interfaces.dto.response.warehouse.UserWarehouseResponseDTO;
import org.lvtn.mws.interfaces.mapper.WarehouseWebMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/users/{userId}/warehouses")
@RequiredArgsConstructor
public class WarehouseAccessController {

    private final AssignWarehouseAccessUseCase    assignWarehouseAccessUseCase;
    private final GetUserWarehouseAccessUseCase   getUserWarehouseAccessUseCase;
    private final WarehouseWebMapper              mapper;

    @GetMapping
    @PreAuthorize("hasAuthority('USER_VIEW')")
    public ResponseEntity<UserWarehouseResponseDTO> getUserWarehouses(@PathVariable String userId) {
        var warehouses = getUserWarehouseAccessUseCase.execute(userId);
        var ids = warehouses.stream()
                .map(w -> w.getId())
                .collect(Collectors.toList());
        return ResponseEntity.ok(new UserWarehouseResponseDTO(userId, ids));
    }

    @PutMapping
    @PreAuthorize("hasAuthority('USER_ASSIGN_WAREHOUSE')")
    public ResponseEntity<Void> assignWarehouses(
            @PathVariable String userId,
            @Valid @RequestBody AssignWarehouseRequestDTO request) {
        assignWarehouseAccessUseCase.execute(userId, request.getWarehouseIds());
        return ResponseEntity.ok().build();
    }
}