package org.lvtn.mws.application.usecases.stocktake;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.StocktakeDetail;
import org.lvtn.mws.domain.service.StocktakeDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ApproveStocktakeLineUseCase {

    private final StocktakeDomainService stocktakeDomainService;

    @Transactional
    public StocktakeDetail execute(String detailId, String approvedBy, String reason) {
        return stocktakeDomainService.approveLine(detailId, approvedBy, reason);
    }
}
