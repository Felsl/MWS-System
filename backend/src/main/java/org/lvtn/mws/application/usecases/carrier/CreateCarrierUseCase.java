package org.lvtn.mws.application.usecases.carrier;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.Carrier;
import org.lvtn.mws.domain.service.CarrierDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CreateCarrierUseCase {

    private final CarrierDomainService carrierDomainService;

    public Carrier execute(String code, String name, String shippingFeeRule, Carrier.Status status) {
        return carrierDomainService.create(code, name, shippingFeeRule, status);
    }
}
