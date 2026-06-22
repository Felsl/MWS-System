package org.lvtn.mws.application.usecases.stockmovement;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.StockMovement;
import org.lvtn.mws.domain.repository.IStockMovementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Truy vết thẻ kho (Audit Trail): theo sản phẩm hoặc theo chứng từ gốc. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetStockMovementsUseCase {

    private final IStockMovementRepository stockMovementRepository;

    public List<StockMovement> byProduct(String productId) {
        return stockMovementRepository.findByProductId(productId);
    }

    public List<StockMovement> byReference(String referenceType, String referenceId) {
        return stockMovementRepository.findByReference(referenceType, referenceId);
    }
}
