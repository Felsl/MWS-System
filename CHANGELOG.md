# CHANGELOG — MWS System

Ghi lại các thay đổi do rà soát so với spec (LVTN). Mỗi batch là một nhóm thay đổi tự đóng, compile/test độc lập được.

---

## [Batch 1] Hoàn thiện Transfer + Optimistic Locking cho lô hàng

### #1 — Hoàn thiện module Điều chuyển nội bộ

- **Đổi tên trạng thái `REQUESTED` → `PENDING_APPROVAL`** trong toàn bộ luồng transfer
  (`TransferOrder`, `TransferOrderDomainService`, `TransferOrderController`), thống nhất với
  đặt tên đã chốt.
  - ⚠️ **Lưu ý vận hành:** cột `transfer_orders.status` lưu enum dạng STRING. Nếu DB đã có
    dữ liệu cũ mang giá trị `'REQUESTED'`, cần chạy:
    `UPDATE transfer_orders SET status = 'PENDING_APPROVAL' WHERE status = 'REQUESTED';`
    (Bản `demo.sql` không seed giá trị này nên cài mới không bị ảnh hưởng.)
- **Thêm luồng Huỷ / Từ chối có nhả giữ chỗ ảo** (trước đây enum có `CANCELLED`/`REJECTED`
  nhưng không có usecase nào dùng → phiếu đã giữ chỗ mà huỷ sẽ rò rỉ `reserved_quantity`):
  - `TransferOrderDomainService.cancelTransfer(id)` và `rejectTransfer(id, rejectedBy)`:
    nếu phiếu đang ở `PENDING_APPROVAL`/`APPROVED` thì nhả `reserved_quantity` kho nguồn
    (đối xứng với lúc gửi duyệt), rồi mới chuyển trạng thái.
  - UseCase mới: `CancelTransferOrderUseCase`, `RejectTransferOrderUseCase` (đều `@Transactional`).
  - DTO mới: `RejectTransferRequest` (`rejectedBy`).
  - Endpoint mới: `POST /api/v1/transfer-orders/{id}/reject`, `POST /api/v1/transfer-orders/{id}/cancel`.

**File thêm:** `application/usecases/transfer/CancelTransferOrderUseCase.java`,
`application/usecases/transfer/RejectTransferOrderUseCase.java`,
`interfaces/dto/request/transfer/RejectTransferRequest.java`
**File sửa:** `domain/model/TransferOrder.java`, `domain/service/TransferOrderDomainService.java`,
`interfaces/rest/TransferOrderController.java`

### #4 — Optimistic Locking cho `inventory_batches`

- Thêm cột `version INT NOT NULL DEFAULT 0` vào `inventory_batches` (`db/demo.sql`).
  - ⚠️ DB đang chạy cần: `ALTER TABLE inventory_batches ADD COLUMN version INT NOT NULL DEFAULT 0;`
- Thêm `@Version` vào `InventoryBatchEntity`; mang field `version` xuyên qua domain model
  `InventoryBatch` (field + Builder + getter) và `InventoryBatchInfraMapper` (map 2 chiều) —
  giống đúng cách `Inventory` đã làm, để optimistic lock hoạt động thật với pattern
  map-domain-sang-entity-mới rồi `save()`.
- Hệ quả: putaway (nhập), picking commit (xuất), adjustment (kiểm kê) và transfer dispatch/complete
  khi cùng cộng/trừ một lô sẽ ném `OptimisticLockingFailureException`
  (đã được `GlobalExceptionHandler` xử lý sẵn).
- 📌 **Theo dõi (follow-up):** các usecase đụng batch của transfer
  (`DispatchTransferShipmentUseCase`, `CompleteTransferReceiptUseCase`) hiện chưa có vòng lặp
  retry như `ReserveStockUseCase`. Nên bổ sung retry (tối đa 3 lần) ở batch sau cho đồng nhất.

---

## [Batch 2] CRUD Đối tác + Gợi ý Putaway

### #2 — CRUD Supplier / Customer / Carrier (full CRUD)

- **Supplier** và **Customer**: dựng đủ tầng theo đúng pattern `ProductCategory`
  (domain model + IRepository + DomainService + Entity + InfraMapper + JpaRepository +
  RepositoryImpl + 5 UseCase Create/GetAll/GetById/Update/Delete + Request/Response DTO +
  WebMapper + Controller). Soft-delete bằng `deleted_at`. Bean domain service đăng ký trong
  `DomainServiceConfig`.
  - Supplier fields: code, name, contactName, phone, email, address, status, deletedAt.
  - Customer fields: code, name, taxCode, phone, email, address, status, createdAt, deletedAt.
- **Carrier**: trước đó chỉ có Create + GetAll → bổ sung **GetById / Update / Delete** cho đủ CRUD.
  - `Carrier.update(name, shippingFeeRule)` (code immutable); xóa mềm bằng `deactivate()`
    (status → INACTIVE, vì bảng `carriers` không có cột `deleted_at`).
  - Thêm `UpdateCarrierRequest`, 3 usecase, thay mới `CarrierController` đủ 5 endpoint.
- **Phân quyền:** tất cả endpoint của 3 controller gắn
  `@PreAuthorize("hasAuthority('MASTER_PARTNER_MANAGE')")` — đúng mã quyền trong ma trận spec.
  - ⚠️ **demo.sql KHÔNG seed permission nào.** Để các endpoint này chạy được, chạy thêm
    `db/seed_partner_permission.sql` (tạo quyền `MASTER_PARTNER_MANAGE` + gán cho role
    ADMIN/PURCHASER/SALESMAN). Nếu chưa seed quyền này, mọi request sẽ trả 403.

**File thêm:** ~32 file dưới `domain/{model,repository,service}`,
`infrastructure/persistence/{entity,mapper,repository/supplier,repository/customer}`,
`application/usecases/{supplier,customer,carrier}`, `interfaces/{dto,mapper,rest}`,
`db/seed_partner_permission.sql`
**File sửa:** `infrastructure/config/DomainServiceConfig.java`,
`domain/model/Carrier.java`, `domain/service/CarrierDomainService.java`,
`interfaces/rest/CarrierController.java`

### #7 — Gợi ý ô kệ Putaway

- `SuggestPutawayBinsUseCase` (read-only): cho `productId` + `warehouseId`, trả danh sách ô kệ
  xếp theo ưu tiên **SAME_PRODUCT** (ô đang chứa cùng sản phẩm → gom hàng) →
  **EMPTY** (ô trống) → **OTHER** (ô chứa sản phẩm khác); trong mỗi nhóm sắp theo toạ độ
  zone/aisle/rack/bin. Chỉ là **gợi ý** — GRN/Transfer-receive vẫn cho nhập tay ô bất kỳ.
- Endpoint `GET /api/v1/putaway/suggestions?productId=&warehouseId=`, gắn quyền
  `INBOUND_COMPLETE_GRN` (đã có trong ma trận spec).

**File thêm:** `application/usecases/putaway/SuggestPutawayBinsUseCase.java`,
`interfaces/dto/response/putaway/PutawaySuggestionResponse.java`,
`interfaces/rest/PutawayController.java`

---

## [Batch 2] Hardening Auth (#6) + Phân quyền (#5)

### #6 — Auth: refresh (stateless) + logout + change-password

- `ITokenProvider`/`JwtTokenProvider`: thêm `generateRefreshToken(userId, username)` (TTL dài,
  claim `type=refresh`) và `extractTokenType`; access token nay mang claim `type=access`.
- `application.properties`: thêm `jwt.refresh-expiration-ms=604800000` (7 ngày).
- `LoginResponse`: thêm field `refreshToken`; `LoginUseCase` phát kèm refresh token.
- UseCase mới: `RefreshTokenUseCase` (xác thực refresh token, tải lại role+permissions mới nhất,
  cấp access mới + XOAY refresh), `ChangePasswordUseCase` (xác minh mật khẩu cũ, ràng buộc độ dài).
- `UserDomainService.changePassword(id, encodedNewPassword)`.
- DTO mới: `RefreshTokenRequest`, `ChangePasswordRequest`.
- Endpoint mới trong `AuthController`: `POST /auth/refresh`, `POST /auth/logout`, `POST /auth/change-password`.
- `SecurityConfig`: mở `permitAll` cho `/api/v1/auth/refresh`.
- ⚠️ **Hạn chế đã biết (stateless):** server không lưu refresh token nên KHÔNG thu hồi được trước hạn;
  `/logout` thực chất là client tự xoá token. Khắc phục khi chuyển sang bảng lưu refresh token (làm sau).

### #5 — Phân quyền: danh mục permission + bộ gói role ("kiểm qua permission")

- Mô hình chốt: **mọi `@PreAuthorize` kiểm theo permission** (`hasAuthority`); role chỉ là "bó" permission
  để gán user cho nhanh.
- File mới `db/seed_permissions_full.sql`: **catalog ĐẦY ĐỦ 46 permission hạt mịn** cho toàn project
  + bộ gói role gợi ý (ADMIN=tất cả; WH_MANAGER; STOREKEEPER; PICKER; PURCHASER; SALESMAN). Idempotent,
  gán theo `p.code`. **Bao trùm** `seed_partner_permission.sql` và block seed quyền trong `demo.sql`.
- File mới `db/RBAC_COVERAGE_PROPOSAL.md`: bảng ánh xạ **endpoint nghiệp vụ → permission** + danh sách
  endpoint cần `@WarehouseScoped`.
- 📌 **Đính chính:** controller admin/master vốn ĐÃ guard đúng (class-level + method-level `@PreAuthorize`);
  cảnh báo "gắn lệch verb" ở review trước là **sai do công cụ liệt kê**, KHÔNG sửa các controller đó.
- ⏳ **Chưa gắn annotation cho controller nghiệp vụ** (PurchaseOrder/GoodsReceipt/Inventory/Picking/
  SalesOrder/Shipment/Stocktake/Adjustment/Transfer/Product/ProductCategory/Notification/StockMovement):
  chờ chốt danh mục rồi gắn — bước cơ học, làm ngay khi OK.

---

## [Dự kiến — chưa làm]

- **#5 (phần còn lại)** Gắn `@PreAuthorize` + `@WarehouseScoped` cho controller nghiệp vụ (sau khi chốt seed).
- **#3** Test concurrency/FEFO (làm sau cùng).
