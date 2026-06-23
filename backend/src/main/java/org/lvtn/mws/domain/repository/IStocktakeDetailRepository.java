package org.lvtn.mws.domain.repository;

import org.lvtn.mws.domain.model.StocktakeDetail;
import java.util.List;
import java.util.Optional;

public interface IStocktakeDetailRepository {
    StocktakeDetail save(StocktakeDetail detail);
    void saveAll(List<StocktakeDetail> details);
    Optional<StocktakeDetail> findById(String id);
    List<StocktakeDetail> findBySessionId(String sessionId);
}
