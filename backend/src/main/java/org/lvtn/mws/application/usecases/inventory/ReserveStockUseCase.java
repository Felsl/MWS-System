package org.lvtn.mws.application.usecases.inventory;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.Inventory;
import org.lvtn.mws.domain.service.InventoryDomainService;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Giữ chỗ tồn kho có chống tranh chấp (optimistic locking + retry).
 *
 * <p>Điểm mấu chốt về TRANSACTION: vòng lặp retry phải nằm NGOÀI transaction.
 * OptimisticLockingFailureException chỉ phát sinh lúc COMMIT (khi Hibernate flush
 * câu UPDATE ... WHERE version = ?). Nếu bọc cả vòng lặp trong một @Transactional
 * thì tới lúc commit vòng lặp đã kết thúc → không bao giờ bắt được để thử lại
 * (retry vô dụng). Vì vậy mỗi LẦN THỬ là một transaction độc lập chạy qua
 * TransactionTemplate: commit ngay trong execute(), lỗi version bật ra đồng bộ
 * để catch → thử lại: đọc lại tồn mới nhất rồi giành tiếp phần còn lại.
 *
 * <p>Nhờ vậy khi nhiều luồng cùng giành hàng, mọi đơn vị còn khả dụng đều được
 * giữ chỗ (không kẹt oan), đồng thời KHÔNG bao giờ giữ vượt tồn (không oversell).
 */
@Service
@RequiredArgsConstructor
public class ReserveStockUseCase {

    private static final int MAX_ATTEMPTS = 10;

    private final InventoryDomainService domainService;
    private final TransactionTemplate transactionTemplate;

    public Inventory execute(String productId, String warehouseId, int quantity) {
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                // Mỗi lần thử = 1 transaction riêng; commit (và version-check) xảy ra
                // ngay trong execute() nên xung đột optimistic lock bật ra tại đây.
                return transactionTemplate.execute(status ->
                        domainService.reserve(productId, warehouseId, quantity));
            } catch (OptimisticLockingFailureException ex) {
                if (attempt == MAX_ATTEMPTS) throw ex;
                try {
                    Thread.sleep(15L * attempt); // backoff tăng dần, giảm va chạm dồn
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw ex;
                }
            }
        }
        throw new IllegalStateException("Không thể giữ chỗ sau " + MAX_ATTEMPTS + " lần thử");
    }
}
