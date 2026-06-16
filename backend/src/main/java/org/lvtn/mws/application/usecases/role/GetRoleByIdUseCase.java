package org.lvtn.mws.application.usecases.role;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.Role;
import org.lvtn.mws.domain.service.RoleDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetRoleByIdUseCase {
    private final RoleDomainService roleDomainService;

    public Role execute(String id) {
        return roleDomainService.findById(id);
    }
}
