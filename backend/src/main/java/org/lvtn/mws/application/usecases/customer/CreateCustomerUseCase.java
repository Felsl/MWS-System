package org.lvtn.mws.application.usecases.customer;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.Customer;
import org.lvtn.mws.domain.service.CustomerDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @RequiredArgsConstructor @Transactional
public class CreateCustomerUseCase {
    private final CustomerDomainService service;
    public Customer execute(String code, String name, String taxCode,
                            String phone, String email, String address) {
        return service.create(code, name, taxCode, phone, email, address);
    }
}
