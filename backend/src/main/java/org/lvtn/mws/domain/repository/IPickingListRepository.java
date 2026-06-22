package org.lvtn.mws.domain.repository;

import org.lvtn.mws.domain.model.PickingList;

import java.util.List;
import java.util.Optional;

public interface IPickingListRepository {
    PickingList save(PickingList pickingList);
    Optional<PickingList> findById(String id);
    Optional<PickingList> findBySoId(String soId);
    List<PickingList> findAll();
    /** Tìm 1 dòng chi tiết để đối soát quét mã (trả về aggregate chứa dòng đó). */
    Optional<PickingList> findByDetailId(String pickingListDetailId);
}
