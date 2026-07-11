package org.lvtn.mws.application.usecases.stocktake;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.StocktakeSession;
import org.lvtn.mws.domain.service.StocktakeDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.lvtn.mws.infrastructure.security.scope.CreationDateScope;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetStocktakeByIdUseCase {

    private final StocktakeDomainService stocktakeDomainService;
    private final CreationDateScope creationDateScope;

    public StocktakeSession execute(String id) {
        StocktakeSession rec = stocktakeDomainService.findById(id);
        creationDateScope.assertVisible(rec.getCreatedAt());
        return rec;
    }
}
