package org.lvtn.mws.application.usecases.goodsreceipt;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.GoodsReceipt;
import org.lvtn.mws.domain.service.GoodsReceiptDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetGoodsReceiptByIdUseCase {
    private final GoodsReceiptDomainService domainService;

    public GoodsReceipt execute(String grnId) {
        return domainService.findById(grnId);
    }
}
