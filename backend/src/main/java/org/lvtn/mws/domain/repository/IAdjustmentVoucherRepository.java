package org.lvtn.mws.domain.repository;

import org.lvtn.mws.domain.model.AdjustmentVoucher;
import java.util.List;
import java.util.Optional;

public interface IAdjustmentVoucherRepository {
    AdjustmentVoucher save(AdjustmentVoucher voucher);
    Optional<AdjustmentVoucher> findById(String id);
    List<AdjustmentVoucher> findAll();
    List<AdjustmentVoucher> findBySessionId(String sessionId);
    boolean existsByVoucherNumber(String voucherNumber);
}
