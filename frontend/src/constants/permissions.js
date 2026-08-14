// Mã quyền THẬT lấy từ @PreAuthorize trong BE (Phase 1).
export const P = {
  USER_VIEW: 'USER_VIEW', USER_CREATE: 'USER_CREATE',
  USER_UPDATE: 'USER_UPDATE', USER_DELETE: 'USER_DELETE',
  USER_ASSIGN_WAREHOUSE: 'USER_ASSIGN_WAREHOUSE',
  ROLE_VIEW: 'ROLE_VIEW', ROLE_CREATE: 'ROLE_CREATE',
  ROLE_UPDATE: 'ROLE_UPDATE', ROLE_DELETE: 'ROLE_DELETE',
  PERMISSION_VIEW: 'PERMISSION_VIEW', PERMISSION_CREATE: 'PERMISSION_CREATE',
  PERMISSION_UPDATE: 'PERMISSION_UPDATE', PERMISSION_DELETE: 'PERMISSION_DELETE',
  WAREHOUSE_VIEW: 'WAREHOUSE_VIEW', WAREHOUSE_CREATE: 'WAREHOUSE_CREATE',
  WAREHOUSE_UPDATE: 'WAREHOUSE_UPDATE', WAREHOUSE_DELETE: 'WAREHOUSE_DELETE',
  MASTER_PRODUCT_VIEW: 'MASTER_PRODUCT_VIEW', MASTER_PRODUCT_MANAGE: 'MASTER_PRODUCT_MANAGE',
  MASTER_PARTNER_MANAGE: 'MASTER_PARTNER_MANAGE',
  INBOUND_VIEW_PO: 'INBOUND_VIEW_PO', INBOUND_CREATE_PO: 'INBOUND_CREATE_PO',
  INBOUND_SUBMIT_PO: 'INBOUND_SUBMIT_PO',
  INBOUND_APPROVE_PO: 'INBOUND_APPROVE_PO',
  INBOUND_VIEW_GRN: 'INBOUND_VIEW_GRN', INBOUND_CREATE_GRN: 'INBOUND_CREATE_GRN',
  INBOUND_COMPLETE_GRN: 'INBOUND_COMPLETE_GRN',
  OUTBOUND_VIEW_SO: 'OUTBOUND_VIEW_SO', OUTBOUND_CREATE_SO: 'OUTBOUND_CREATE_SO',
  OUTBOUND_VIEW: 'OUTBOUND_VIEW', OUTBOUND_PICK: 'OUTBOUND_PICK', OUTBOUND_SHIP: 'OUTBOUND_SHIP',
  TRANSFER_VIEW: 'TRANSFER_VIEW', TRANSFER_CREATE: 'TRANSFER_CREATE',
  TRANSFER_APPROVE: 'TRANSFER_APPROVE', TRANSFER_DISPATCH: 'TRANSFER_DISPATCH',
  TRANSFER_RECEIVE: 'TRANSFER_RECEIVE',
  STOCKTAKE_VIEW: 'STOCKTAKE_VIEW', STOCKTAKE_MANAGE: 'STOCKTAKE_MANAGE',
  STOCKTAKE_APPROVE: 'STOCKTAKE_APPROVE',
  ADJUSTMENT_VIEW: 'ADJUSTMENT_VIEW', ADJUSTMENT_APPROVE: 'ADJUSTMENT_APPROVE',
  INVENTORY_VIEW: 'INVENTORY_VIEW', INVENTORY_ADJUST: 'INVENTORY_ADJUST',
  AUDIT_VIEW_MOVEMENTS: 'AUDIT_VIEW_MOVEMENTS',
  NOTIF_READ: 'NOTIF_READ',
}

/**
 * Nhãn tiếng Việt cho từng mã quyền.
 *
 * Dùng cho tooltip "Cần quyền: ..." trên nút bị khoá (xem components/Can.jsx).
 * Hiện mã trần kiểu MASTER_PRODUCT_MANAGE cho thủ kho thì cũng như không nói gì.
 *
 * Khoá của map này PHẢI trùng giá trị trong P ở trên — có test bảo vệ
 * (src/__smoke__/smoke.test.jsx) để không ai thêm quyền mới mà quên nhãn.
 */
export const PERM_LABELS = {
  USER_VIEW: 'Xem người dùng',
  USER_CREATE: 'Tạo người dùng',
  USER_UPDATE: 'Sửa người dùng',
  USER_DELETE: 'Xoá người dùng',
  USER_ASSIGN_WAREHOUSE: 'Gán kho cho người dùng',
  ROLE_VIEW: 'Xem vai trò',
  ROLE_CREATE: 'Tạo vai trò',
  ROLE_UPDATE: 'Sửa vai trò',
  ROLE_DELETE: 'Xoá vai trò',
  PERMISSION_VIEW: 'Xem quyền hạn',
  PERMISSION_CREATE: 'Tạo quyền hạn',
  PERMISSION_UPDATE: 'Sửa quyền hạn',
  PERMISSION_DELETE: 'Xoá quyền hạn',
  WAREHOUSE_VIEW: 'Xem kho',
  WAREHOUSE_CREATE: 'Tạo kho',
  WAREHOUSE_UPDATE: 'Sửa kho',
  WAREHOUSE_DELETE: 'Đóng kho',
  MASTER_PRODUCT_VIEW: 'Xem sản phẩm',
  MASTER_PRODUCT_MANAGE: 'Quản lý sản phẩm',
  MASTER_PARTNER_MANAGE: 'Quản lý đối tác',
  INBOUND_VIEW_PO: 'Xem đơn mua hàng',
  INBOUND_CREATE_PO: 'Tạo đơn mua hàng',
  INBOUND_SUBMIT_PO: 'Trình phê duyệt đơn mua',
  INBOUND_APPROVE_PO: 'Duyệt đơn mua hàng',
  INBOUND_VIEW_GRN: 'Xem phiếu nhập kho',
  INBOUND_CREATE_GRN: 'Tạo phiếu nhập kho',
  INBOUND_COMPLETE_GRN: 'Hoàn tất phiếu nhập kho',
  OUTBOUND_VIEW_SO: 'Xem đơn bán hàng',
  OUTBOUND_CREATE_SO: 'Tạo đơn bán hàng',
  OUTBOUND_VIEW: 'Xem lệnh lấy hàng / vận đơn',
  OUTBOUND_PICK: 'Lấy hàng',
  OUTBOUND_SHIP: 'Xuất & giao hàng',
  TRANSFER_VIEW: 'Xem phiếu điều chuyển',
  TRANSFER_CREATE: 'Tạo phiếu điều chuyển',
  TRANSFER_APPROVE: 'Duyệt phiếu điều chuyển',
  TRANSFER_DISPATCH: 'Gửi hàng điều chuyển',
  TRANSFER_RECEIVE: 'Nhận hàng điều chuyển',
  STOCKTAKE_VIEW: 'Xem phiên kiểm kê',
  STOCKTAKE_MANAGE: 'Quản lý kiểm kê',
  STOCKTAKE_APPROVE: 'Duyệt kiểm kê',
  ADJUSTMENT_VIEW: 'Xem phiếu điều chỉnh',
  ADJUSTMENT_APPROVE: 'Duyệt phiếu điều chỉnh',
  INVENTORY_VIEW: 'Xem tồn kho',
  INVENTORY_ADJUST: 'Điều chỉnh tồn kho',
  AUDIT_VIEW_MOVEMENTS: 'Xem thẻ kho',
  NOTIF_READ: 'Đọc thông báo',
}

/** Nhãn tiếng Việt của một mã quyền; chưa có nhãn thì trả về chính mã đó. */
export function permLabel(code) {
  return PERM_LABELS[code] || code
}
