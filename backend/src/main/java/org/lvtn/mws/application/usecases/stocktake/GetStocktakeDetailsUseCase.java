package org.lvtn.mws.application.usecases.stocktake;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.StocktakeDetail;
import org.lvtn.mws.domain.service.StocktakeDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetStocktakeDetailsUseCase {

    private final StocktakeDomainService stocktakeDomainService;

    public List<StocktakeDetail> execute(String sessionId) {
        return stocktakeDomainService.findDetails(sessionId);
    }
}
