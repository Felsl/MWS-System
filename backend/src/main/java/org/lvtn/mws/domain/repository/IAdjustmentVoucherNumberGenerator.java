package org.lvtn.mws.domain.repository;

/** Port sinh voucher_number hiển thị (vd ADJ-yyyyMMdd-####). */
public interface IAdjustmentVoucherNumberGenerator {
    String next();
}
