package org.lvtn.mws.application.usecases.role;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.service.RoleDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteRoleUseCase {
    private final RoleDomainService roleDomainService;

    @Transactional
    public void execute(String id) {
        roleDomainService.delete(id);
    }
}
