package org.lvtn.mws.application.usecases.salesorder;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.application.ports.INotificationRecipientResolver;
import org.lvtn.mws.application.usecases.notification.CreateNotificationUseCase;
import org.lvtn.mws.domain.model.Notification;
import org.lvtn.mws.domain.model.SalesOrder;
import org.lvtn.mws.domain.model.StockDemand;
import org.lvtn.mws.domain.repository.IStockDemandRepository;
import org.lvtn.mws.domain.service.SalesOrderDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Gửi duyệt / Xác nhận đơn -> ALLOCATED hoặc PARTIALLY_ALLOCATED (phân bổ một phần).
 * [Bán vượt tồn] Nếu phát sinh nhu cầu (backorder) -> báo bộ phận mua (INBOUND_CREATE_PO).
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AllocateSalesOrderUseCase {

    private static final String PERM_PURCHASER = "INBOUND_CREATE_PO";

    private final SalesOrderDomainService salesOrderDomainService;
    private final IStockDemandRepository stockDemandRepository;
    private final INotificationRecipientResolver recipientResolver;
    private final CreateNotificationUseCase createNotificationUseCase;

    public SalesOrder execute(String soId) {
        SalesOrder so = salesOrderDomainService.allocate(soId);

        List<StockDemand> demands = stockDemandRepository.findOpenBySoId(soId);
        if (!demands.isEmpty()) {
            List<String> recipients = recipientResolver.resolveByPermission(PERM_PURCHASER);
            int totalShort = demands.stream().mapToInt(StockDemand::getQuantityShort).sum();
            String title = "Cần nhập gấp hàng cho đơn bán";
            String message = String.format(
                    "Đơn %s thiếu %d sản phẩm (%d dòng) — vui lòng tạo PO bổ sung.",
                    so.getSoNumber() == null ? soId : so.getSoNumber(), totalShort, demands.size());
            createNotificationUseCase.createForUsers(
                    recipients, title, message, Notification.Type.WARNING, "SALES_ORDER", soId);
        }
        return so;
    }
}
