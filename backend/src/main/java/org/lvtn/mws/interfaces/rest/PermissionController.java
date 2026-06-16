package org.lvtn.mws.interfaces.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.lvtn.mws.application.usecases.permission.*;
import org.lvtn.mws.interfaces.dto.request.permission.CreatePermissionRequest;
import org.lvtn.mws.interfaces.dto.request.permission.UpdatePermissionRequest;
import org.lvtn.mws.interfaces.dto.response.permission.PermissionResponse;
import org.lvtn.mws.interfaces.mapper.PermissionWebMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('PERMISSION_VIEW')")
public class PermissionController {

    private final CreatePermissionUseCase createUseCase;
    private final GetAllPermissionsUseCase getAllUseCase;
    private final UpdatePermissionUseCase  updateUseCase;
    private final DeletePermissionUseCase  deleteUseCase;
    private final PermissionWebMapper      mapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('PERMISSION_CREATE')")
    public PermissionResponse create(@Valid @RequestBody CreatePermissionRequest request) {
        return mapper.toResponse(createUseCase.execute(
                request.getCode(), request.getName(), request.getModule()));
    }

    @GetMapping
    public List<PermissionResponse> getAll() {
        return getAllUseCase.execute().stream()
                .map(mapper::toResponse).collect(Collectors.toList());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PERMISSION_UPDATE')")
    public PermissionResponse update(@PathVariable String id,
                                     @Valid @RequestBody UpdatePermissionRequest request) {
        return mapper.toResponse(updateUseCase.execute(
                id, request.getCode(), request.getName(), request.getModule()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('PERMISSION_DELETE')")
    public void delete(@PathVariable String id) {
        deleteUseCase.execute(id);
    }
}
