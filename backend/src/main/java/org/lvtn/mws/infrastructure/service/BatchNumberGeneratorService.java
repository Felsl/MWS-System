package org.lvtn.mws.infrastructure.service;

import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Generates batch numbers in the format: BATCH-YYYYMMDD-XXXX
 * The sequence resets each application restart (stateless per-day counter).
 * For production, use a DB sequence or Redis counter instead.
 */
@Service
public class BatchNumberGeneratorService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private final AtomicInteger counter = new AtomicInteger(0);

    public String generate() {
        String date = LocalDate.now().format(DATE_FMT);
        int seq = counter.incrementAndGet();
        return String.format("BATCH-%s-%04d", date, seq);
    }
}
