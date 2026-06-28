package org.lvtn.mws.application.usecases.supplier;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.service.SupplierDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @RequiredArgsConstructor @Transactional
public class DeleteSupplierUseCase {
    private final SupplierDomainService service;
    public void execute(String id) { service.delete(id); }
}
