package org.lvtn.mws.application.usecases.role;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.application.ports.IIdGenerator;
import org.lvtn.mws.domain.model.Role;
import org.lvtn.mws.domain.service.RoleDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateRoleUseCase {
    private final RoleDomainService roleDomainService;
    private final IIdGenerator      idGenerator;

    @Transactional
    public Role execute(String code, String name, String description) {
        return roleDomainService.create(idGenerator.generate(), code, name, description);
    }
}
