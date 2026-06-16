package org.lvtn.mws.application.usecases.permission;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.Permission;
import org.lvtn.mws.domain.service.PermissionDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdatePermissionUseCase {
    private final PermissionDomainService permissionDomainService;

    @Transactional
    public Permission execute(String id, String code, String name, String module) {
        return permissionDomainService.update(id, code, name, module);
    }
}
