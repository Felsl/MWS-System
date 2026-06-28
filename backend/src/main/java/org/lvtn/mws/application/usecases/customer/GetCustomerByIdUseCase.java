package org.lvtn.mws.application.usecases.customer;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.Customer;
import org.lvtn.mws.domain.service.CustomerDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @RequiredArgsConstructor @Transactional(readOnly = true)
public class GetCustomerByIdUseCase {
    private final CustomerDomainService service;
    public Customer execute(String id) { return service.findById(id); }
}
