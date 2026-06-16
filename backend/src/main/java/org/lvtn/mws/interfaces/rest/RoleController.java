package org.lvtn.mws.interfaces.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.lvtn.mws.application.usecases.role.CreateRoleUseCase;
import org.lvtn.mws.application.usecases.role.*;
import org.lvtn.mws.interfaces.dto.request.role.AssignPermissionsRequest;
import org.lvtn.mws.interfaces.dto.request.role.CreateRoleRequest;
import org.lvtn.mws.interfaces.dto.request.role.UpdateRoleRequest;
import org.lvtn.mws.interfaces.dto.response.role.RoleResponse;
import org.lvtn.mws.interfaces.mapper.RoleWebMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_VIEW')")
public class RoleController {

    private final CreateRoleUseCase createUseCase;
    private final GetAllRolesUseCase             getAllUseCase;
    private final GetRoleByIdUseCase             getByIdUseCase;
    private final UpdateRoleUseCase              updateUseCase;
    private final DeleteRoleUseCase              deleteUseCase;
    private final AssignPermissionsToRoleUseCase assignPermissionsUseCase;
    private final RoleWebMapper                  mapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ROLE_CREATE')")
    public RoleResponse create(@Valid @RequestBody CreateRoleRequest request) {
        return mapper.toResponse(createUseCase.execute(
                request.getCode(), request.getName(), request.getDescription()));
    }

    @GetMapping
    public List<RoleResponse> getAll() {
        return getAllUseCase.execute().stream()
                .map(mapper::toResponse).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public RoleResponse getById(@PathVariable String id) {
        return mapper.toResponse(getByIdUseCase.execute(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_UPDATE')")
    public RoleResponse update(@PathVariable String id,
                               @Valid @RequestBody UpdateRoleRequest request) {
        return mapper.toResponse(updateUseCase.execute(
                id, request.getCode(), request.getName(), request.getDescription()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('ROLE_DELETE')")
    public void delete(@PathVariable String id) {
        deleteUseCase.execute(id);
    }

    @PutMapping("/{id}/permissions")
    @PreAuthorize("hasAuthority('ROLE_UPDATE')")
    public RoleResponse assignPermissions(@PathVariable String id,
                                          @Valid @RequestBody AssignPermissionsRequest request) {
        return mapper.toResponse(assignPermissionsUseCase.execute(id, request.getPermissionIds()));
    }
}
