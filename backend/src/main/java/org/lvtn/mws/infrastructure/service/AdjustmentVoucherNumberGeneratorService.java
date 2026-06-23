package org.lvtn.mws.infrastructure.service;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.repository.IAdjustmentVoucherNumberGenerator;
import org.lvtn.mws.infrastructure.persistence.repository.adjustment.JpaAdjustmentVoucherRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/** Sinh voucher_number dạng ADJ-yyyyMMdd-#### (#### = số thứ tự trong ngày). */
@Service
@RequiredArgsConstructor
public class AdjustmentVoucherNumberGeneratorService implements IAdjustmentVoucherNumberGenerator {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyyMMdd");
    private final JpaAdjustmentVoucherRepository jpa;

    @Override
    public String next() {
        String prefix = "ADJ-" + LocalDate.now().format(DATE) + "-";
        long seq = jpa.countByVoucherNumberStartingWith(prefix) + 1;
        return prefix + String.format("%04d", seq);
    }
}
