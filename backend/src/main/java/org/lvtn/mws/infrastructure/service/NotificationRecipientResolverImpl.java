package org.lvtn.mws.infrastructure.service;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.application.ports.INotificationRecipientResolver;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * [GIAI ĐOẠN 7] Giải người nhận theo permission code (+ phạm vi kho) bằng native SQL.
 * Join users → role_permissions → permissions; lọc user ACTIVE & chưa xoá mềm.
 * Dùng JdbcTemplate (spring-boot-starter-jdbc) để không ràng buộc tên class entity.
 */
@Component
@RequiredArgsConstructor
public class NotificationRecipientResolverImpl implements INotificationRecipientResolver {

    private final JdbcTemplate jdbc;

    private static final String BASE =
            "SELECT DISTINCT u.id FROM users u " +
            "JOIN role_permissions rp ON rp.role_id = u.role_id " +
            "JOIN permissions p ON p.id = rp.permission_id " +
            "WHERE p.code = ? AND u.status = 'ACTIVE' AND u.deleted_at IS NULL";

    @Override
    public List<String> resolveByPermission(String permissionCode) {
        return jdbc.queryForList(BASE, String.class, permissionCode);
    }

    @Override
    public List<String> resolveByPermissionAndWarehouse(String permissionCode, String warehouseId) {
        String sql = BASE +
                " AND EXISTS (SELECT 1 FROM user_warehouse_access uwa " +
                "             WHERE uwa.user_id = u.id AND uwa.warehouse_id = ?)";
        return jdbc.queryForList(sql, String.class, permissionCode, warehouseId);
    }
}
