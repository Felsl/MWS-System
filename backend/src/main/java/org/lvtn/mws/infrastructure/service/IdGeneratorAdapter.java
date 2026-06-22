package org.lvtn.mws.infrastructure.service;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.repository.IIdGenerator;
import org.springframework.stereotype.Component;

/** Adapter nối domain port IIdGenerator với IdGeneratorService có sẵn (UUID 8 ký tự). */
@Component
@RequiredArgsConstructor
public class IdGeneratorAdapter implements IIdGenerator {

    private final IdGeneratorService idGeneratorService;

    @Override
    public String generate() {
        return idGeneratorService.generate();
    }
}
