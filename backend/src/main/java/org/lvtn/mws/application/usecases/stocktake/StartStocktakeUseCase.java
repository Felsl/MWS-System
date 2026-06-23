package org.lvtn.mws.application.usecases.stocktake;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.StocktakeSession;
import org.lvtn.mws.domain.service.StocktakeDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Bắt đầu kiểm kê: đóng băng kho + chụp ảnh tồn (trong 1 giao dịch). */
@Service
@RequiredArgsConstructor
public class StartStocktakeUseCase {

    private final StocktakeDomainService stocktakeDomainService;

    @Transactional
    public StocktakeSession execute(String warehouseId, String createdBy) {
        return stocktakeDomainService.startStocktake(warehouseId, createdBy);
    }
}
