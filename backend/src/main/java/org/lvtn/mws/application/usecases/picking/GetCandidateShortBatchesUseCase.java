package org.lvtn.mws.application.usecases.picking;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.InventoryBatch;
import org.lvtn.mws.domain.repository.ISupplierRepository;
import org.lvtn.mws.domain.service.PickingDomainService;
import org.lvtn.mws.interfaces.dto.response.picking.CandidateBatchResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** [Báo thiếu] Danh sách lô ứng viên (cùng SP + NCC + kho, ACTIVE) cho một dòng nhặt. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetCandidateShortBatchesUseCase {

    private final PickingDomainService pickingDomainService;
    private final ISupplierRepository supplierRepository;

    public List<CandidateBatchResponse> execute(String pickingListDetailId) {
        Map<String, String> nameCache = new HashMap<>();
        return pickingDomainService.candidateBatchesForShort(pickingListDetailId).stream()
                .map(b -> new CandidateBatchResponse(
                        b.getId(), b.getBatchNumber(), b.getSupplierId(),
                        b.getSupplierId() == null ? null
                                : nameCache.computeIfAbsent(b.getSupplierId(),
                                    id -> supplierRepository.findById(id).map(s -> s.getName()).orElse(id)),
                        b.getBinLocationId(), b.getQuantity()))
                .toList();
    }
}
