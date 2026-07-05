# MWS Frontend — Auth + Master Data (Giai đoạn 1)

Frontend cho hệ WMS. Stack: **React 19 + Vite + Ant Design + TanStack Query + Axios + React Router**, JS thuần.
Gọi BE qua **Vite dev proxy** (không cần BE bật CORS khi dev).

## Chạy dev
```bash
npm install
npm run dev        # http://localhost:5173  →  proxy /api, /ws  →  http://localhost:8080
```
Đảm bảo backend Spring Boot chạy ở `localhost:8080`. Đổi target trong `vite.config.js` nếu khác.

## Build production
Khi không dùng proxy, đặt base URL của BE trong `.env`:
```
VITE_API_BASE_URL=https://api.your-domain.com
```
```bash
npm run build && npm run preview
```

## Cấu trúc
```
src/
  api/            axios client (interceptor refresh 401) + 1 file API / module
  auth/           AuthContext (token + user + permissions), useAuth
  components/     AppLayout, ProtectedRoute, Can, CrudResource (CRUD tái sử dụng)
  constants/      permissions.js (mã quyền THẬT của BE), units.js
  features/       auth, dashboard, users, roles, permissions, warehouses, products, categories, partners
  routes/         khai báo route gắn quyền
```

## Đã có ở Giai đoạn 1
- Đăng nhập JWT + tự refresh khi 401, khôi phục phiên khi F5 (gọi `/me`).
- Layout + sidebar tự ẩn mục theo quyền; `<Can permission>` ẩn/hiện nút; route chặn theo quyền.
- Người dùng: CRUD + gán vai trò + **gán kho** (warehouse access).
- Vai trò: CRUD + **ma trận phân quyền** theo module.
- Quyền hạn: CRUD.
- Kho: CRUD (xoá mềm) + **sinh ô kệ hàng loạt** (Zone→Aisle→Rack→Bin) + xoá ô kệ.
- Sản phẩm: CRUD + **phân trang server** + tìm keyword.
- Nhóm sản phẩm, Đối tác (NCC / Khách hàng / Vận chuyển): CRUD.

## Lưu ý kỹ thuật (khớp với BE thật)
- Mã quyền dùng đúng theo `@PreAuthorize` của BE: `USER_*`, `ROLE_*`, `PERMISSION_*`, `WAREHOUSE_*`,
  `MASTER_PRODUCT_VIEW/MANAGE`, `MASTER_PARTNER_MANAGE` (KHÁC với tài liệu thiết kế .txt).
- ID là chuỗi ULID → luôn xử lý dạng string, không parseInt.
- `getErrorMessage()` gom lỗi field từ chuỗi `message` của BE (BE chưa trả lỗi theo field).
- Warehouse scope do BE tự lọc → FE không gửi header kho.

## Điểm cần xác nhận lại với team BE (mình đã ghi lại khi đọc code)
1. **Gán kho cho user bị trùng 2 endpoint**: `PUT /api/v1/users/{id}/warehouse-access` (UserController) và
   `PUT /api/v1/users/{userId}/warehouses` (WarehouseAccessController). FE hiện dùng cái sau
   (`.../warehouses`). Cần chốt body chuẩn: hiện gửi `{ "warehouseIds": [...] }` — nhờ BE xác nhận tên field
   và shape response của `GET .../warehouses` (đang đọc linh hoạt `warehouseIds` hoặc `warehouses[].id`).
2. **Bulk sinh ô kệ**: FE gửi `{ zones: [{ zone, aisles:[{aisle, racks:[{rack, bins:[]}]}] }] }`.
   Nhờ xác nhận đúng cấu trúc `ZoneConfig/AisleConfig/RackConfig` và field kết quả trả về (đang đọc `created`).
3. **Product không có upload ảnh** ở API hiện tại — nếu sau này thêm ảnh (Cloudinary) cần route riêng.
4. Bảo mật: `jwt.secret`, `cloudinary.api-secret`, mật khẩu DB đang commit trong repo — nên rút ra biến môi trường và rotate.
