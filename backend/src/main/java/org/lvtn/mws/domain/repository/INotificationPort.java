package org.lvtn.mws.domain.repository;

/**
 * PORT — gửi cảnh báo hệ thống (notifications).
 * Hiện có impl mặc định ghi log (LoggingNotificationAdapter). Thay bằng module
 * notifications thật khi đã sẵn sàng.
 */
public interface INotificationPort {

    /** Cảnh báo hao hụt dọc đường gửi cho Admin/Manager. */
    void sendTransferDiscrepancyAlert(String transferId,
                                      String transferNumber,
                                      String productId,
                                      int expectedQty,
                                      int receivedQty,
                                      int lostQty);
}
