package org.lvtn.mws.application.usecases.permission;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.service.PermissionDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeletePermissionUseCase {
    private final PermissionDomainService permissionDomainService;

    @Transactional
    public void execute(String id) {
        permissionDomainService.delete(id);
    }
}
