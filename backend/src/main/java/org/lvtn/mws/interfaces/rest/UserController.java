package org.lvtn.mws.interfaces.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.lvtn.mws.application.usecases.user.*;
import org.lvtn.mws.interfaces.dto.request.user.AssignWarehouseRequest;
import org.lvtn.mws.interfaces.dto.request.user.CreateUserRequest;
import org.lvtn.mws.interfaces.dto.request.user.UpdateUserRequest;
import org.lvtn.mws.interfaces.dto.response.user.UserResponse;
import org.lvtn.mws.interfaces.mapper.UserWebMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('USER_VIEW')")
public class UserController {

    private final CreateUserUseCase              createUseCase;
    private final GetUserByIdUseCase             getByIdUseCase;
    private final GetAllUsersUseCase             getAllUseCase;
    private final UpdateUserUseCase              updateUseCase;
    private final DeleteUserUseCase              deleteUseCase;
    private final AssignWarehouseAccessUseCase   assignWarehouseUseCase;
    private final GetUserWarehouseAccessUseCase  getWarehouseUseCase;
    private final UserWebMapper                  mapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('USER_CREATE')")
    public UserResponse create(@Valid @RequestBody CreateUserRequest request) {
        return mapper.toResponse(createUseCase.execute(
                request.getUsername(), request.getPassword(),
                request.getFullName(), request.getEmail(),
                request.getPhone(), request.getRoleId()));
    }

    @GetMapping("/{id}")
    public UserResponse getById(@PathVariable String id) {
        return mapper.toResponse(getByIdUseCase.execute(id));
    }

    @GetMapping
    public List<UserResponse> getAll() {
        return getAllUseCase.execute().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_UPDATE')")
    public UserResponse update(@PathVariable String id,
                               @Valid @RequestBody UpdateUserRequest request) {
        return mapper.toResponse(updateUseCase.execute(
                id, request.getFullName(), request.getEmail(),
                request.getPhone(), request.getRoleId()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('USER_DELETE')")
    public void delete(@PathVariable String id) {
        deleteUseCase.execute(id);
    }

    @PutMapping("/{id}/warehouse-access")
    @PreAuthorize("hasAuthority('USER_UPDATE')")
    public ResponseEntity<Void> assignWarehouses(@PathVariable String id,
                                                  @Valid @RequestBody AssignWarehouseRequest request) {
        assignWarehouseUseCase.execute(id, request.getWarehouseIds());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/warehouse-access")
    public ResponseEntity<?> getWarehouseAccess(@PathVariable String id) {
        return ResponseEntity.ok(getWarehouseUseCase.execute(id));
    }
}
