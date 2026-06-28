package org.lvtn.mws.application.usecases.customer;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.Customer;
import org.lvtn.mws.domain.service.CustomerDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service @RequiredArgsConstructor @Transactional(readOnly = true)
public class GetAllCustomersUseCase {
    private final CustomerDomainService service;
    public List<Customer> execute() { return service.findAll(); }
}
