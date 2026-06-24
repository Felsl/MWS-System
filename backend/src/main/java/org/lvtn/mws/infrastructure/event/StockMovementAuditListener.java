package org.lvtn.mws.infrastructure.event;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.application.event.StockMovementEvent;
import org.lvtn.mws.domain.repository.IStockMovementRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * [GIAI ĐOẠN 7] Ghi thẻ kho (Audit Trail) SAU KHI transaction nghiệp vụ commit thành công.
 *
 * @TransactionalEventListener(AFTER_COMMIT): chỉ chạy khi đơn gốc (GRN/SO/Transfer) đã xuống DB
 * 100%. Nếu đơn rollback thì listener KHÔNG chạy → không tạo thẻ kho "ma".
 *
 * Lưu ý đánh đổi: vì chạy SAU commit, lỗi ghi thẻ kho ở đây KHÔNG rollback nghiệp vụ gốc.
 * Do đó chỉ log lỗi (không ném) để không ảnh hưởng response đã trả cho client; cần giám sát log.
 * Mỗi save chạy trong transaction riêng của Spring Data.
 */
@Component
@RequiredArgsConstructor
public class StockMovementAuditListener {

    private static final Logger log = LoggerFactory.getLogger(StockMovementAuditListener.class);

    private final IStockMovementRepository stockMovementRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onStockMovement(StockMovementEvent event) {
        try {
            stockMovementRepository.save(event.movement());
        } catch (Exception ex) {
            log.error("[AUDIT] Ghi thẻ kho thất bại sau commit (productId={}, ref={}:{}): {}",
                    event.movement().getProductId(),
                    event.movement().getReferenceType(),
                    event.movement().getReferenceId(),
                    ex.getMessage(), ex);
        }
    }
}
