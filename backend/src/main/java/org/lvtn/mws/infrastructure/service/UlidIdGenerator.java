package org.lvtn.mws.infrastructure.service;

import org.lvtn.mws.application.ports.IIdGenerator;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.Instant;

/**
 * Simple ULID-like ID generator producing 20-char alphanumeric IDs.
 * Matches VARCHAR(20) PK in the schema.
 */
@Component
public class UlidIdGenerator implements IIdGenerator {

    private static final String CHARS = "0123456789ABCDEFGHJKMNPQRSTVWXYZ";
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    public String generate() {
        long timestamp = Instant.now().toEpochMilli();
        StringBuilder sb = new StringBuilder(20);
        // 10 chars timestamp
        for (int i = 9; i >= 0; i--) {
            sb.insert(0, CHARS.charAt((int)(timestamp % 32)));
            timestamp /= 32;
        }
        // 10 chars random
        for (int i = 0; i < 10; i++) {
            sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }
        return sb.toString();
    }
}
