package org.lvtn.mws.application.usecases.stocktake;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.StocktakeSession;
import org.lvtn.mws.domain.service.StocktakeDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetStocktakeByIdUseCase {

    private final StocktakeDomainService stocktakeDomainService;

    public StocktakeSession execute(String id) {
        return stocktakeDomainService.findById(id);
    }
}
