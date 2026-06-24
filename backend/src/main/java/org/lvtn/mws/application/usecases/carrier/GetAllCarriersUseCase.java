package org.lvtn.mws.application.usecases.carrier;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.Carrier;
import org.lvtn.mws.domain.service.CarrierDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetAllCarriersUseCase {

    private final CarrierDomainService carrierDomainService;

    public List<Carrier> execute() {
        return carrierDomainService.findAll();
    }
}
