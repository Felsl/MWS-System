package org.lvtn.mws.application.usecases.goodsreceipt;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.GoodsReceiptDetail;
import org.lvtn.mws.domain.service.GoodsReceiptDomainService;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Sửa NSX + HSD của một dòng phiếu nhập (đã hoặc chưa complete) và đồng bộ
 * xuống inventory_batches khớp. Không đụng SL/ô kệ/số lô/giá.
 *
 * @Transactional cho toàn bộ (grd + batches cùng commit); retry optimistic-lock
 * vì InventoryBatchEntity có @Version — cùng lô có thể đang được picking/adjustment
 * chỉnh sửa song song.
 */
@Service
@RequiredArgsConstructor
public class UpdateGoodsReceiptDetailDatesUseCase {

    private final GoodsReceiptDomainService domainService;

    @Transactional
    public GoodsReceiptDetail execute(String grnId, String detailId,
                                      LocalDate manufacturedDate, LocalDate expiryDate) {
        int maxAttempts = 3;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return domainService.updateDetailDates(grnId, detailId, manufacturedDate, expiryDate);
            } catch (OptimisticLockingFailureException ex) {
                if (attempt == maxAttempts) throw ex;
                try { Thread.sleep(100L * attempt); }
                catch (InterruptedException ie) { Thread.currentThread().interrupt(); throw ex; }
            }
        }
        throw new IllegalStateException("Không thể cập nhật NSX/HSD sau " + maxAttempts + " lần thử");
    }
}
