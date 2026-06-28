package org.lvtn.mws.application.usecases.supplier;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.Supplier;
import org.lvtn.mws.domain.service.SupplierDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @RequiredArgsConstructor @Transactional(readOnly = true)
public class GetSupplierByIdUseCase {
    private final SupplierDomainService service;
    public Supplier execute(String id) { return service.findById(id); }
}
