package org.lvtn.mws.application.usecases.permission;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.Permission;
import org.lvtn.mws.domain.service.PermissionDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetAllPermissionsUseCase {
    private final PermissionDomainService permissionDomainService;

    public List<Permission> execute() {
        return permissionDomainService.findAll();
    }
}
