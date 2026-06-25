# RBAC COVERAGE — ĐỀ XUẤT (mục #5)

## Vì sao đây là bản đề xuất, chưa gắn thẳng vào code

Các controller **admin/master** (Warehouse, User, Role, Permission, Supplier, Customer,
Carrier, Notification) **đã được bảo vệ đúng** bằng `@PreAuthorize` cấp class + override
cấp method — KHÔNG cần sửa. (Lần rà trước mình nghi "gắn lệch verb" nhưng đó là lỗi của
script liệt kê do không tính annotation cấp class; đọc source thì chúng đều đúng.)

Phần thật sự còn hở là các controller **nghiệp vụ** bên dưới. Để gắn `@PreAuthorize` cho
chúng cần **permission code có thật trong DB** và **được gán cho role phù hợp**. Nhưng
`db/demo.sql` ghi rõ *"seed roles ở script riêng của bạn"* và repo KHÔNG chứa script đó.
Nếu gắn code đoán mà code không khớp seed → endpoint **403 vĩnh viễn**, không role nào vào
được (rủi ro tự khoá đã cảnh báo). Vì vậy: chốt mapping + chạy seed trước, rồi mới gắn.

Quy trình an toàn: (1) bạn duyệt/sửa bảng dưới cho khớp ma trận spec → (2) chạy
`db/seed_rbac_coverage.sql` → (3) gắn `@PreAuthorize` (mình làm, ~30 phút cơ học) hoặc tự gắn.

Convention đặt tên theo spec: `MODULE_ACTION` (vd `INBOUND_CREATE_PO`). Role tham chiếu:
`ADMIN, WH_MANAGER, STOREKEEPER, PICKER, PURCHASER, SALESMAN` (gộp từ seed + spec).

## Bảng ánh xạ endpoint nghiệp vụ → permission đề xuất

| Controller | Method & Path | Permission đề xuất | Role gợi ý |
|---|---|---|---|
| PurchaseOrder | POST /purchase-orders | INBOUND_CREATE_PO | ADMIN, PURCHASER |
| PurchaseOrder | POST /purchase-orders/{id}/submit-review | INBOUND_CREATE_PO | ADMIN, PURCHASER |
| PurchaseOrder | POST /purchase-orders/{id}/submit-approval | INBOUND_CREATE_PO | ADMIN, PURCHASER |
| PurchaseOrder | POST /purchase-orders/{id}/approve | INBOUND_APPROVE_PO | ADMIN, WH_MANAGER |
| PurchaseOrder | POST /purchase-orders/{id}/reject | INBOUND_APPROVE_PO | (đã gắn) |
| PurchaseOrder | GET /purchase-orders/{id} | INBOUND_VIEW_PO | ADMIN, PURCHASER, WH_MANAGER |
| GoodsReceipt | POST /goods-receipts | INBOUND_COMPLETE_GRN | ADMIN, STOREKEEPER |
| GoodsReceipt | POST /goods-receipts/{id}/complete | INBOUND_COMPLETE_GRN | ADMIN, STOREKEEPER |
| GoodsReceipt | GET /goods-receipts/{id} | INBOUND_VIEW_GRN | ADMIN, STOREKEEPER, WH_MANAGER |
| Inventory | GET /inventory, /inventory/warehouse/{id} | INVENTORY_VIEW | ADMIN, WH_MANAGER, STOREKEEPER |
| Inventory | GET /inventory/batches, /batches/allocate | INVENTORY_VIEW | ADMIN, WH_MANAGER, STOREKEEPER |
| Inventory | POST /inventory/init, /inventory/batches | INVENTORY_ADJUST | ADMIN, WH_MANAGER |
| Inventory | POST /inventory/reserve, /release, /commit | INVENTORY_ADJUST | ADMIN (nội bộ; cân nhắc chặn ở mức service) |
| Inventory | PATCH /inventory/batches/{id}/status | INVENTORY_ADJUST | ADMIN, WH_MANAGER |
| PickingList | POST /picking-lists, /{id}/assign, /{id}/complete | OUTBOUND_PICK | ADMIN, PICKER, STOREKEEPER |
| PickingList | POST /details/{id}/confirm, /details/{id}/short | OUTBOUND_PICK | ADMIN, PICKER |
| PickingList | GET /picking-lists, /picking-lists/{id} | OUTBOUND_VIEW | ADMIN, PICKER, WH_MANAGER |
| SalesOrder | POST /sales-orders | OUTBOUND_CREATE_SO | ADMIN, SALESMAN |
| SalesOrder | POST /sales-orders/{id}/allocate | OUTBOUND_PICK | ADMIN, WH_MANAGER, PICKER |
| SalesOrder | POST /sales-orders/{id}/cancel | OUTBOUND_CREATE_SO | ADMIN, SALESMAN |
| SalesOrder | GET /sales-orders, /sales-orders/{id} | OUTBOUND_VIEW_SO | ADMIN, SALESMAN, WH_MANAGER |
| Shipment | POST /shipments, /{id}/tracking, /{id}/ship, /{id}/deliver | OUTBOUND_SHIP | ADMIN, WH_MANAGER, STOREKEEPER |
| Shipment | GET /shipments, /shipments/{id} | OUTBOUND_VIEW | ADMIN, WH_MANAGER |
| Stocktake | POST /stocktakes, /details/{id}/count, /{id}/complete | STOCKTAKE_MANAGE | ADMIN, WH_MANAGER, STOREKEEPER |
| Stocktake | POST /details/{id}/approve-line | STOCKTAKE_APPROVE | ADMIN, WH_MANAGER |
| Stocktake | GET /stocktakes, /stocktakes/{id} | STOCKTAKE_VIEW | ADMIN, WH_MANAGER, STOREKEEPER |
| Adjustment | POST /adjustment-vouchers/{id}/approve | ADJUSTMENT_APPROVE | ADMIN, WH_MANAGER |
| Adjustment | GET /adjustment-vouchers, /{id} | ADJUSTMENT_VIEW | ADMIN, WH_MANAGER |
| TransferOrder | POST /transfer-orders, /{id}/request-approval | TRANSFER_CREATE | ADMIN, WH_MANAGER, STOREKEEPER |
| TransferOrder | POST /{id}/approve, /{id}/reject | TRANSFER_APPROVE | ADMIN, WH_MANAGER |
| TransferOrder | POST /{id}/dispatch | TRANSFER_DISPATCH | ADMIN, STOREKEEPER |
| TransferOrder | POST /{id}/complete | TRANSFER_RECEIVE | ADMIN, STOREKEEPER |
| TransferOrder | POST /{id}/cancel | TRANSFER_CREATE | ADMIN, WH_MANAGER |
| TransferOrder | GET /transfer-orders, /{id} | TRANSFER_VIEW | ADMIN, WH_MANAGER, STOREKEEPER |
| Product | POST/PUT/DELETE /products | MASTER_PRODUCT_MANAGE | ADMIN |
| Product | GET /products, /products/{id} | MASTER_PRODUCT_VIEW | tất cả role vận hành |
| ProductCategory | POST/PUT/DELETE /product-categories | MASTER_PRODUCT_MANAGE | ADMIN |
| ProductCategory | GET /product-categories, /{id} | MASTER_PRODUCT_VIEW | tất cả role vận hành |
| Notification | PATCH /{id}/read, /read-all, GET /unread-count | NOTIF_READ | tất cả role |
| StockMovement | GET /stock-movements (bản chưa guard) | AUDIT_VIEW_MOVEMENTS | ADMIN, WH_MANAGER |

> Ghi chú Adjustment: code đã có `UnauthorizedAdjustmentException` → có thể đang kiểm
> quyền duyệt ở tầng service. Kiểm tra để tránh chặn hai lớp mâu thuẫn.

## @WarehouseScoped — lọc dữ liệu theo kho

Hiện `@WarehouseScoped` chỉ dùng ở 4 chỗ. Nên bổ sung cho các **API đọc/thao tác theo kho**
của role không phải ADMIN, để thủ kho kho A không thấy/không tác động dữ liệu kho B:
- GET /inventory, /inventory/warehouse/{id}, /inventory/batches
- GET /goods-receipts/{id}, /picking-lists, /shipments, /stocktakes, /transfer-orders
- Các thao tác ghi gắn warehouseId (GRN complete, picking, dispatch...) nên kiểm
  `user_warehouse_access` trước khi cho thực thi.

Đây là phần cần xem cơ chế resolve warehouseId hiện tại của `WarehouseScopeAspect` để gắn
đúng tham số — mình sẽ làm cùng lúc khi gắn `@PreAuthorize`, sau khi bạn chốt bảng trên.
