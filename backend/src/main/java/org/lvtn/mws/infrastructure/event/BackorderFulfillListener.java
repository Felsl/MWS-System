package org.lvtn.mws.infrastructure.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lvtn.mws.application.event.StockMovementEvent;
import org.lvtn.mws.application.usecases.salesorder.FulfillBackordersUseCase;
import org.lvtn.mws.domain.model.StockMovement;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.EnumSet;
import java.util.Set;

/**
 * [Bán vượt tồn] Khi tồn TĂNG (hàng về) và đã commit, thử bù các nhu cầu backorder OPEN
 * của (sản phẩm, kho) đó. Chạy SAU COMMIT để chắc chắn tồn/lô mới đã hiện diện.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BackorderFulfillListener {

    private static final Set<StockMovement.MovementType> INCREASING = EnumSet.of(
            StockMovement.MovementType.IN,
            StockMovement.MovementType.ADJUST_IN,
            StockMovement.MovementType.TRANSFER_IN);

    private final FulfillBackordersUseCase fulfillBackordersUseCase;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onStockMovement(StockMovementEvent event) {
        StockMovement m = event.movement();
        if (m.getMovementType() == null || !INCREASING.contains(m.getMovementType())) return;
        try {
            fulfillBackordersUseCase.execute(m.getProductId(), m.getWarehouseId());
        } catch (Exception ex) {
            log.warn("[Backorder] Bù nhu cầu thất bại cho SP {} kho {}: {}",
                    m.getProductId(), m.getWarehouseId(), ex.getMessage());
        }
    }
}
