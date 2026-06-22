package org.lvtn.mws.infrastructure.service;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.repository.ISalesOrderNumberGenerator;
import org.lvtn.mws.infrastructure.persistence.repository.salesorder.JpaSalesOrderRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/** Sinh so_number dạng SO-yyyyMMdd-#### (#### = số thứ tự trong ngày). */
@Service
@RequiredArgsConstructor
public class SoNumberGeneratorService implements ISalesOrderNumberGenerator {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyyMMdd");
    private final JpaSalesOrderRepository jpa;

    @Override
    public String next() {
        String prefix = "SO-" + LocalDate.now().format(DATE) + "-";
        long seq = jpa.countBySoNumberStartingWith(prefix) + 1;
        return prefix + String.format("%04d", seq);
    }
}
