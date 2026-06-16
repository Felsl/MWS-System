package org.lvtn.mws.application.usecases.warehouse;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.service.WarehouseDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteBinLocationUseCase {

    private final WarehouseDomainService warehouseDomainService;

    @Transactional
    public void execute(String id) {
        warehouseDomainService.deleteBinLocation(id);
    }
}
