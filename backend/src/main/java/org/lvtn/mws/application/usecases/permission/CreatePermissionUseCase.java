package org.lvtn.mws.application.usecases.permission;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.application.ports.IIdGenerator;
import org.lvtn.mws.domain.model.Permission;
import org.lvtn.mws.domain.service.PermissionDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreatePermissionUseCase {
    private final PermissionDomainService permissionDomainService;
    private final IIdGenerator            idGenerator;

    @Transactional
    public Permission execute(String code, String name, String module) {
        return permissionDomainService.create(idGenerator.generate(), code, name, module);
    }
}
