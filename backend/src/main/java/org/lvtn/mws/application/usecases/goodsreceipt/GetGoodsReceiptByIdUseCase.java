package org.lvtn.mws.application.usecases.goodsreceipt;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.GoodsReceipt;
import org.lvtn.mws.domain.service.GoodsReceiptDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.lvtn.mws.infrastructure.security.scope.CreationDateScope;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetGoodsReceiptByIdUseCase {
    private final GoodsReceiptDomainService domainService;
    private final CreationDateScope creationDateScope;

    public GoodsReceipt execute(String grnId) {
        GoodsReceipt rec = domainService.findById(grnId);
        creationDateScope.assertVisible(rec.getReceivedAt());
        return rec;
    }
}
