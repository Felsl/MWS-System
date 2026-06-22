package org.lvtn.mws.interfaces.dto.request.goodsreceipt;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class CreateGoodsReceiptRequest {
    private String poId; // nullable — ad-hoc receipt allowed

    @NotBlank(message = "warehouseId không được trống")
    private String warehouseId;

    private String note;

    @Valid
    @NotEmpty(message = "Phiếu nhập phải có ít nhất một dòng hàng")
    private List<GoodsReceiptLineRequest> lines;
}
