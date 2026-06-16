package org.lvtn.mws.domain.repository;

import java.util.List;

public interface IUserWarehouseAccessRepository {
    void assignWarehouses(String userId, List<String> warehouseIds);
    void revokeAllForUser(String userId);
    List<String> findWarehouseIdsByUserId(String userId);
    List<String> findWarehouseIdsByUsername(String username);
    void revokeWarehouse(String userId, String warehouseId);
}
