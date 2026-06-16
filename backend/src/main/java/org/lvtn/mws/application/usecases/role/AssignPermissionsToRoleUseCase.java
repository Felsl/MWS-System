package org.lvtn.mws.application.usecases.role;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.Role;
import org.lvtn.mws.domain.service.RoleDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AssignPermissionsToRoleUseCase {
    private final RoleDomainService roleDomainService;

    @Transactional
    public Role execute(String roleId, List<String> permissionIds) {
        return roleDomainService.assignPermissions(roleId, permissionIds);
    }
}
