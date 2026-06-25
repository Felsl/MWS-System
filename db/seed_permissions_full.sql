-- =========================================================================
-- SEED: TOÀN BỘ PERMISSION CỦA PROJECT (catalog đầy đủ, hạt mịn) + bộ gói role.
-- Nguồn sự thật duy nhất cho phân quyền. An toàn chạy lại (INSERT IGNORE).
-- Chạy SAU khi đã seed bảng `roles`.
--
-- Ghi chú thiết kế:
--  - Mọi @PreAuthorize đều kiểm theo PERMISSION (hasAuthority), KHÔNG kiểm theo role.
--    Role chỉ là "bó" permission để gán user cho nhanh (mục 2 dưới — gợi ý, sửa tuỳ ý).
--  - Tất cả lệnh GÁN dùng tra theo p.code (không hardcode permission_id) nên vẫn đúng
--    kể cả khi một permission đã tồn tại sẵn với id khác (vd các quyền đã seed ở GĐ7).
--  - code là UNIQUE: nếu code đã có, dòng INSERT IGNORE bị bỏ qua (giữ nguyên bản cũ) — vô hại.
--  - File này bao trùm và thay thế: seed_partner_permission.sql, seed_rbac_coverage.sql
--    và block seed quyền trong demo.sql (không cần chạy chúng nữa, nhưng chạy lại cũng vô hại).
-- =========================================================================

-- ========================= 1) DANH MỤC PERMISSION =========================
INSERT IGNORE INTO permissions (id, code, name, module) VALUES
  -- USER & RBAC ----------------------------------------------------------
  ('P_USER_VIEW',      'USER_VIEW',              'Xem người dùng',                 'USER'),
  ('P_USER_CREATE',    'USER_CREATE',            'Tạo người dùng',                 'USER'),
  ('P_USER_UPDATE',    'USER_UPDATE',            'Sửa người dùng',                 'USER'),
  ('P_USER_DELETE',    'USER_DELETE',            'Xoá người dùng',                 'USER'),
  ('P_USER_ASSIGN_WH', 'USER_ASSIGN_WAREHOUSE',  'Gán quyền tiếp cận kho',         'USER'),
  ('P_ROLE_VIEW',      'ROLE_VIEW',              'Xem vai trò',                    'USER'),
  ('P_ROLE_CREATE',    'ROLE_CREATE',            'Tạo vai trò',                    'USER'),
  ('P_ROLE_UPDATE',    'ROLE_UPDATE',            'Sửa vai trò / gán quyền',        'USER'),
  ('P_ROLE_DELETE',    'ROLE_DELETE',            'Xoá vai trò',                    'USER'),
  ('P_PERM_VIEW',      'PERMISSION_VIEW',        'Xem quyền',                      'USER'),
  ('P_PERM_CREATE',    'PERMISSION_CREATE',      'Tạo quyền',                      'USER'),
  ('P_PERM_UPDATE',    'PERMISSION_UPDATE',      'Sửa quyền',                      'USER'),
  ('P_PERM_DELETE',    'PERMISSION_DELETE',      'Xoá quyền',                      'USER'),
  -- MASTER DATA ----------------------------------------------------------
  ('P_WH_VIEW',        'WAREHOUSE_VIEW',         'Xem kho / ô kệ',                 'MASTER_DATA'),
  ('P_WH_CREATE',      'WAREHOUSE_CREATE',       'Tạo kho / sinh ô kệ',            'MASTER_DATA'),
  ('P_WH_UPDATE',      'WAREHOUSE_UPDATE',       'Sửa kho',                        'MASTER_DATA'),
  ('P_WH_DELETE',      'WAREHOUSE_DELETE',       'Xoá kho / ô kệ',                 'MASTER_DATA'),
  ('P_PARTNER_MNG',    'MASTER_PARTNER_MANAGE',  'Quản lý đối tác (NCC/KH/ĐVVC)',  'MASTER_DATA'),
  ('P_PRD_MANAGE',     'MASTER_PRODUCT_MANAGE',  'Quản lý sản phẩm / danh mục',    'MASTER_DATA'),
  ('P_PRD_VIEW',       'MASTER_PRODUCT_VIEW',    'Xem sản phẩm / danh mục',        'MASTER_DATA'),
  -- INBOUND --------------------------------------------------------------
  ('P_INB_CREATE_PO',  'INBOUND_CREATE_PO',      'Tạo/sửa/trình duyệt phiếu mua',  'INBOUND'),
  ('P_INB_APPROVE_PO', 'INBOUND_APPROVE_PO',     'Duyệt/từ chối phiếu mua',        'INBOUND'),
  ('P_INB_VIEW_PO',    'INBOUND_VIEW_PO',        'Xem phiếu mua',                  'INBOUND'),
  ('P_INB_CREATE_GRN', 'INBOUND_CREATE_GRN',     'Tạo phiếu nhập kho',             'INBOUND'),
  ('P_INB_GRN',        'INBOUND_COMPLETE_GRN',   'Hoàn tất nhập kho / cất hàng',   'INBOUND'),
  ('P_INB_VIEW_GRN',   'INBOUND_VIEW_GRN',       'Xem phiếu nhập kho',             'INBOUND'),
  -- INVENTORY ------------------------------------------------------------
  ('P_INV_VIEW',       'INVENTORY_VIEW',         'Xem tồn kho / lô',               'INVENTORY'),
  ('P_INV_ADJUST',     'INVENTORY_ADJUST',       'Khởi tạo/điều chỉnh tồn-lô',     'INVENTORY'),
  -- OUTBOUND -------------------------------------------------------------
  ('P_OUT_CREATE_SO',  'OUTBOUND_CREATE_SO',     'Tạo/huỷ đơn bán',                'OUTBOUND'),
  ('P_OUT_VIEW_SO',    'OUTBOUND_VIEW_SO',       'Xem đơn bán',                    'OUTBOUND'),
  ('P_OUT_PICK',       'OUTBOUND_PICK',          'Gom hàng / nhặt hàng',           'OUTBOUND'),
  ('P_OUT_VIEW',       'OUTBOUND_VIEW',          'Xem lệnh gom / giao vận',        'OUTBOUND'),
  ('P_OUT_SHIP',       'OUTBOUND_SHIP',          'Xuất kho / giao hàng',           'OUTBOUND'),
  -- STOCKTAKE ------------------------------------------------------------
  ('P_STK_MANAGE',     'STOCKTAKE_MANAGE',       'Thực hiện kiểm kê',              'STOCKTAKE'),
  ('P_STK_APPROVE',    'STOCKTAKE_APPROVE',      'Duyệt dòng kiểm kê',             'STOCKTAKE'),
  ('P_STK_VIEW',       'STOCKTAKE_VIEW',         'Xem phiên kiểm kê',              'STOCKTAKE'),
  -- ADJUSTMENT -----------------------------------------------------------
  ('P_ADJ_APPROVE',    'ADJUSTMENT_APPROVE',     'Duyệt phiếu điều chỉnh',         'ADJUSTMENT'),
  ('P_ADJ_VIEW',       'ADJUSTMENT_VIEW',        'Xem phiếu điều chỉnh',           'ADJUSTMENT'),
  -- TRANSFER -------------------------------------------------------------
  ('P_TRF_CREATE',     'TRANSFER_CREATE',        'Tạo/huỷ phiếu điều chuyển',      'TRANSFER'),
  ('P_TRF_APPROVE',    'TRANSFER_APPROVE',       'Duyệt/từ chối điều chuyển',      'TRANSFER'),
  ('P_TRF_DISPATCH',   'TRANSFER_DISPATCH',      'Xuất kho điều chuyển',           'TRANSFER'),
  ('P_TRF_RECEIVE',    'TRANSFER_RECEIVE',       'Nhận hàng điều chuyển',          'TRANSFER'),
  ('P_TRF_VIEW',       'TRANSFER_VIEW',          'Xem phiếu điều chuyển',          'TRANSFER'),
  -- AUDIT & NOTIFICATION -------------------------------------------------
  ('P_AUDIT_VIEW',     'AUDIT_VIEW_MOVEMENTS',   'Xem thẻ kho (audit trail)',      'AUDIT'),
  ('P_NOTIF_READ',     'NOTIF_READ',             'Đọc thông báo',                  'NOTIFICATION'),
  ('P_NOTIF_WH',       'NOTIF_WH_RECEIVE',       'Nhận cảnh báo vận hành kho',     'NOTIFICATION');

-- ===================== 2) BỘ GÓI ROLE (GỢI Ý — sửa tuỳ ý) =================
-- ADMIN: TẤT CẢ permission.
INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p WHERE r.code = 'ADMIN';

-- WH_MANAGER (quản lý kho): duyệt + giám sát toàn cục.
INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r JOIN permissions p ON p.code IN (
  'WAREHOUSE_VIEW','MASTER_PRODUCT_VIEW',
  'INBOUND_APPROVE_PO','INBOUND_VIEW_PO','INBOUND_VIEW_GRN',
  'INVENTORY_VIEW','INVENTORY_ADJUST',
  'OUTBOUND_VIEW','OUTBOUND_VIEW_SO','OUTBOUND_SHIP',
  'STOCKTAKE_MANAGE','STOCKTAKE_APPROVE','STOCKTAKE_VIEW',
  'ADJUSTMENT_APPROVE','ADJUSTMENT_VIEW',
  'TRANSFER_CREATE','TRANSFER_APPROVE','TRANSFER_VIEW',
  'AUDIT_VIEW_MOVEMENTS','NOTIF_READ','NOTIF_WH_RECEIVE'
) WHERE r.code = 'WH_MANAGER';

-- STOREKEEPER (thủ kho): nhập/xuất/điều chuyển vật lý + kiểm kê.
INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r JOIN permissions p ON p.code IN (
  'MASTER_PRODUCT_VIEW',
  'INBOUND_CREATE_GRN','INBOUND_COMPLETE_GRN','INBOUND_VIEW_GRN',
  'INVENTORY_VIEW',
  'OUTBOUND_PICK','OUTBOUND_VIEW','OUTBOUND_SHIP',
  'STOCKTAKE_MANAGE','STOCKTAKE_VIEW',
  'TRANSFER_CREATE','TRANSFER_DISPATCH','TRANSFER_RECEIVE','TRANSFER_VIEW',
  'NOTIF_READ'
) WHERE r.code = 'STOREKEEPER';

-- PICKER (nhân viên nhặt hàng): chỉ gom hàng.
INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r JOIN permissions p ON p.code IN (
  'MASTER_PRODUCT_VIEW','INVENTORY_VIEW','OUTBOUND_PICK','OUTBOUND_VIEW','NOTIF_READ'
) WHERE r.code = 'PICKER';

-- PURCHASER (mua hàng): tạo phiếu mua + danh mục đối tác.
INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r JOIN permissions p ON p.code IN (
  'INBOUND_CREATE_PO','INBOUND_VIEW_PO','MASTER_PARTNER_MANAGE','MASTER_PRODUCT_VIEW','NOTIF_READ'
) WHERE r.code = 'PURCHASER';

-- SALESMAN (bán hàng): tạo đơn bán + danh mục đối tác.
INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r JOIN permissions p ON p.code IN (
  'OUTBOUND_CREATE_SO','OUTBOUND_VIEW_SO','MASTER_PARTNER_MANAGE','MASTER_PRODUCT_VIEW','NOTIF_READ'
) WHERE r.code = 'SALESMAN';
