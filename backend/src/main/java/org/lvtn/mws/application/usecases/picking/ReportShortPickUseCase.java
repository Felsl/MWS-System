package org.lvtn.mws.application.usecases.picking;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.PickingList;
import org.lvtn.mws.domain.service.PickingDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Báo thiếu hàng thực tế tại một dòng nhặt và tự bù phần thiếu từ lô FEFO kế tiếp.
 * @Transactional bao trọn: nếu không còn lô để bù -> rollback toàn bộ.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ReportShortPickUseCase {

    private final PickingDomainService pickingDomainService;

    public PickingList execute(String pickingListDetailId,
                               String scannedBatchNumber,
                               int actualQty,
                               String reason,
                               String confirmedBy) {
        return pickingDomainService.reportShortPick(
                pickingListDetailId, scannedBatchNumber, actualQty, reason, confirmedBy);
    }
}
