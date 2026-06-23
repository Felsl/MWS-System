package org.lvtn.mws.infrastructure.persistence.repository.adjustment;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.AdjustmentVoucher;
import org.lvtn.mws.domain.repository.IAdjustmentVoucherRepository;
import org.lvtn.mws.infrastructure.persistence.mapper.AdjustmentVoucherInfraMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AdjustmentVoucherRepositoryImpl implements IAdjustmentVoucherRepository {

    private final JpaAdjustmentVoucherRepository jpa;
    private final AdjustmentVoucherInfraMapper mapper;

    @Override
    public AdjustmentVoucher save(AdjustmentVoucher voucher) {
        return mapper.toDomain(jpa.save(mapper.toEntity(voucher)));
    }

    @Override
    public Optional<AdjustmentVoucher> findById(String id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<AdjustmentVoucher> findAll() {
        return jpa.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<AdjustmentVoucher> findBySessionId(String sessionId) {
        return jpa.findBySessionId(sessionId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public boolean existsByVoucherNumber(String voucherNumber) {
        return jpa.existsByVoucherNumber(voucherNumber);
    }
}
