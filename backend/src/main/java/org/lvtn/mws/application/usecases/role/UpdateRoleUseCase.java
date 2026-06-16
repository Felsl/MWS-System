package org.lvtn.mws.application.usecases.role;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.Role;
import org.lvtn.mws.domain.service.RoleDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateRoleUseCase {
    private final RoleDomainService roleDomainService;

    @Transactional
    public Role execute(String id, String code, String name, String description) {
        return roleDomainService.update(id, code, name, description);
    }
}
