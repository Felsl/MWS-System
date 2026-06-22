package org.lvtn.mws.application.usecases.goodsreceipt;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.GoodsReceiptDetail;
import org.lvtn.mws.domain.service.GoodsReceiptDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetGoodsReceiptDetailsUseCase {
    private final GoodsReceiptDomainService domainService;

    public List<GoodsReceiptDetail> execute(String grnId) {
        return domainService.findDetails(grnId);
    }
}
