package org.lvtn.mws.application.usecases.role;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.Role;
import org.lvtn.mws.domain.service.RoleDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetAllRolesUseCase {
    private final RoleDomainService roleDomainService;

    public List<Role> execute() {
        return roleDomainService.findAll();
    }
}
