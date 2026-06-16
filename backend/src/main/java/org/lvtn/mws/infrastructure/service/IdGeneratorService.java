package org.lvtn.mws.infrastructure.service;

import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class IdGeneratorService {
    public String generate() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }
}
