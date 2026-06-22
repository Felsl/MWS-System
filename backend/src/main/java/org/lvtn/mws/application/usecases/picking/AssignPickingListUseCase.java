package org.lvtn.mws.application.usecases.picking;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.PickingList;
import org.lvtn.mws.domain.service.PickingDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AssignPickingListUseCase {

    private final PickingDomainService pickingDomainService;

    public PickingList execute(String pickingListId, String userId) {
        return pickingDomainService.assign(pickingListId, userId);
    }
}
