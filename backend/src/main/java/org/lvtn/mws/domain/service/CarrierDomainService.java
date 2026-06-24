package org.lvtn.mws.domain.service;

import org.lvtn.mws.domain.model.Carrier;
import org.lvtn.mws.domain.repository.ICarrierRepository;
import org.lvtn.mws.domain.repository.IIdGenerator;

import java.util.List;

/** Quản lý đơn vị vận chuyển (carriers). Thuần Java. */
public class CarrierDomainService {

    private final ICarrierRepository carrierRepository;
    private final IIdGenerator idGenerator;

    public CarrierDomainService(ICarrierRepository carrierRepository, IIdGenerator idGenerator) {
        this.carrierRepository = carrierRepository;
        this.idGenerator = idGenerator;
    }

    public Carrier create(String code, String name, String shippingFeeRule, Carrier.Status status) {
        if (carrierRepository.existsByCode(code)) {
            throw new IllegalArgumentException("Mã đơn vị vận chuyển đã tồn tại: " + code);
        }
        Carrier carrier = new Carrier.Builder()
                .id(idGenerator.generate())
                .code(code)
                .name(name)
                .shippingFeeRule(shippingFeeRule)
                .status(status != null ? status : Carrier.Status.ACTIVE)
                .build();
        return carrierRepository.save(carrier);
    }

    public List<Carrier> findAll() {
        return carrierRepository.findAll();
    }

    public Carrier findById(String id) {
        return carrierRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn vị vận chuyển: " + id));
    }
}
