package org.lvtn.mws.application.usecases.transfer;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.PickingList;
import org.lvtn.mws.domain.repository.IPickingListRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Lấy lệnh gom hàng của một phiếu điều chuyển. */
@Service
@RequiredArgsConstructor
public class GetTransferPickingUseCase {

    private final IPickingListRepository pickingRepository;

    @Transactional(readOnly = true)
    public PickingList execute(String transferOrderId) {
        return pickingRepository.findByTransferOrderId(transferOrderId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Phiếu điều chuyển chưa có lệnh gom hàng: " + transferOrderId));
    }
}
