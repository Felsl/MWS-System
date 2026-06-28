package org.lvtn.mws.application.usecases.supplier;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.Supplier;
import org.lvtn.mws.domain.service.SupplierDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @RequiredArgsConstructor @Transactional
public class CreateSupplierUseCase {
    private final SupplierDomainService service;
    public Supplier execute(String code, String name, String contactName,
                            String phone, String email, String address) {
        return service.create(code, name, contactName, phone, email, address);
    }
}
