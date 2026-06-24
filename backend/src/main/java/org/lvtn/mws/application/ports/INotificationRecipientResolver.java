package org.lvtn.mws.application.ports;

import java.util.List;

/**
 * [GIAI ĐOẠN 7] Giải danh sách userId người nhận theo PERMISSION CODE (và phạm vi kho khi cần).
 * Adapter ở infrastructure dùng native SQL join users→roles→role_permissions→permissions
 * và user_warehouse_access. Chỉ lấy user đang ACTIVE & chưa xoá mềm.
 */
public interface INotificationRecipientResolver {
    /** Tất cả user (ACTIVE) sở hữu permissionCode — không lọc kho. */
    List<String> resolveByPermission(String permissionCode);

    /** User (ACTIVE) sở hữu permissionCode VÀ có quyền truy cập warehouseId. */
    List<String> resolveByPermissionAndWarehouse(String permissionCode, String warehouseId);
}
