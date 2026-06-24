package org.lvtn.mws.domain.model;

import java.util.List;

/**
 * [GIAI ĐOẠN 7] Kết quả hoàn tất phiếu nhập: phiếu đã COMPLETED kèm danh sách thẻ kho IN
 * đã DỰNG (chưa persist). Tầng UseCase phát sự kiện để ghi thẻ kho AFTER_COMMIT, giữ domain
 * service thuần (không phụ thuộc cơ chế event của Spring).
 */
public record GoodsReceiptCompletion(GoodsReceipt goodsReceipt, List<StockMovement> movements) {
}
