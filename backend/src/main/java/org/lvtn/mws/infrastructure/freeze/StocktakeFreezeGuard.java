package org.lvtn.mws.infrastructure.freeze;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.WarehouseFrozenException;
import org.lvtn.mws.domain.repository.IStocktakeSessionRepository;
import org.springframework.stereotype.Component;

/**
 * Chốt chặn nghiệp vụ: khi một kho đang có phiên kiểm kê FREEZED thì KHÔNG cho phép
 * mọi thao tác làm biến động tồn (Nhập/Xuất/Điều chuyển).
 *
 * ĐÂY là điểm thực thi đáng tin cậy nhất (gọi trực tiếp trong các use case ghi tồn).
 * Xem CHANGELOG để biết các vị trí khuyến nghị chèn lời gọi assertNotFrozen(...).
 */
@Component
@RequiredArgsConstructor
public class StocktakeFreezeGuard {

    private final IStocktakeSessionRepository stocktakeSessionRepository;

    public void assertNotFrozen(String warehouseId) {
        if (warehouseId != null && stocktakeSessionRepository.isWarehouseFrozen(warehouseId)) {
            throw new WarehouseFrozenException(
                    "Kho " + warehouseId + " đang kiểm kê (đóng băng) — tạm khóa mọi biến động tồn kho");
        }
    }
}
