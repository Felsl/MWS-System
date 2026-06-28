package org.lvtn.mws.application.usecases.customer;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.service.CustomerDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @RequiredArgsConstructor @Transactional
public class DeleteCustomerUseCase {
    private final CustomerDomainService service;
    public void execute(String id) { service.delete(id); }
}
