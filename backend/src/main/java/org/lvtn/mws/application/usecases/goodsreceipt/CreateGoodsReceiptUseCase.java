package org.lvtn.mws.application.usecases.goodsreceipt;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.GoodsReceipt;
import org.lvtn.mws.domain.model.GoodsReceiptLineCommand;
import org.lvtn.mws.domain.service.GoodsReceiptDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CreateGoodsReceiptUseCase {
    private final GoodsReceiptDomainService domainService;

    @Transactional
    public GoodsReceipt execute(String poId, String warehouseId, String receivedBy,
                                String note, List<GoodsReceiptLineCommand> lines) {
        return domainService.create(poId, warehouseId, receivedBy, note, lines);
    }
}
