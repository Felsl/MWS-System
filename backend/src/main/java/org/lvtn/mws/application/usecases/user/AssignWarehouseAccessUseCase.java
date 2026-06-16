package org.lvtn.mws.application.usecases.user;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.repository.IUserWarehouseAccessRepository;
import org.lvtn.mws.domain.service.UserDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AssignWarehouseAccessUseCase {

    private final UserDomainService              userDomainService;
    private final IUserWarehouseAccessRepository warehouseAccessRepository;

    @Transactional
    public void execute(String userId, List<String> warehouseIds) {
        userDomainService.findById(userId); // validate user exists
        warehouseIds.forEach(userDomainService::validateWarehouseExists);
        warehouseAccessRepository.revokeAllForUser(userId);
        warehouseAccessRepository.assignWarehouses(userId, warehouseIds);
    }
}
