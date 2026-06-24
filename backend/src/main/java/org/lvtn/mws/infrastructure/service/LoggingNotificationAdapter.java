package org.lvtn.mws.infrastructure.service;

import org.lvtn.mws.domain.repository.INotificationPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Impl mặc định cho INotificationPort: ghi log cảnh báo hao hụt.
 * THAY bằng adapter trỏ về module notifications thật khi sẵn sàng.
 */
@Component
public class LoggingNotificationAdapter implements INotificationPort {

    private static final Logger log = LoggerFactory.getLogger(LoggingNotificationAdapter.class);

    @Override
    public void sendTransferDiscrepancyAlert(String transferId,
                                             String transferNumber,
                                             String productId,
                                             int expectedQty,
                                             int receivedQty,
                                             int lostQty) {
        log.warn("[HAO HỤT ĐIỀU CHUYỂN] phiếu={} ({}), sp={}, gửi={}, nhận={}, thiếu={} -> cần lập biên bản với đơn vị vận chuyển",
                transferNumber, transferId, productId, expectedQty, receivedQty, lostQty);
    }
}
