package org.lvtn.mws.application.usecases.supplier;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.Supplier;
import org.lvtn.mws.domain.service.SupplierDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service @RequiredArgsConstructor @Transactional(readOnly = true)
public class GetAllSuppliersUseCase {
    private final SupplierDomainService service;
    public List<Supplier> execute() { return service.findAll(); }
}
