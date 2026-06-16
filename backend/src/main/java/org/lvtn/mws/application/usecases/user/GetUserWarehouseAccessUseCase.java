package org.lvtn.mws.application.usecases.user;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.Warehouse;
import org.lvtn.mws.domain.repository.IUserWarehouseAccessRepository;
import org.lvtn.mws.domain.repository.IWarehouseRepository;
import org.lvtn.mws.domain.service.UserDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetUserWarehouseAccessUseCase {

    private final UserDomainService              userDomainService;
    private final IUserWarehouseAccessRepository warehouseAccessRepository;
    private final IWarehouseRepository           warehouseRepository;

    public List<Warehouse> execute(String userId) {
        userDomainService.findById(userId);
        List<String> ids = warehouseAccessRepository.findWarehouseIdsByUserId(userId);
        return ids.stream()
                .map(wid -> warehouseRepository.findById(wid)
                        .orElseThrow(() -> new RuntimeException("Warehouse not found: " + wid)))
                .collect(Collectors.toList());
    }
}
