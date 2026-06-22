package org.lvtn.mws.infrastructure.service;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.repository.IShipmentNumberGenerator;
import org.lvtn.mws.infrastructure.persistence.repository.shipment.JpaShipmentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/** Sinh shipment_number dạng SHP-yyyyMMdd-####. */
@Service
@RequiredArgsConstructor
public class ShipmentNumberGeneratorService implements IShipmentNumberGenerator {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyyMMdd");
    private final JpaShipmentRepository jpa;

    @Override
    public String next() {
        String prefix = "SHP-" + LocalDate.now().format(DATE) + "-";
        long seq = jpa.countByShipmentNumberStartingWith(prefix) + 1;
        return prefix + String.format("%04d", seq);
    }
}
