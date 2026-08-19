package org.lvtn.mws.interfaces.dto.request.goodsreceipt;

import lombok.Data;
import java.time.LocalDate;

/** Body cho PATCH /goods-receipts/{grnId}/details/{detailId}/dates.
 *  Cả 2 field đều nullable — cho phép người dùng xóa ngày đã nhập nhầm. */
@Data
public class UpdateDetailDatesRequest {
    private LocalDate manufacturedDate;
    private LocalDate expiryDate;
}
