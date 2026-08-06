===============================================================================
 MWS - MINI WAREHOUSE SYSTEM
 Hệ thống quản lý kho (Warehouse Management System)
===============================================================================

Đồ án tốt nghiệp. MWS là một hệ thống quản lý kho hoàn chỉnh gồm backend REST
(Spring Boot) và frontend SPA (React), quản lý toàn bộ vòng đời hàng hoá trong
kho: danh mục nền, nhập kho, tồn kho theo lô/ô kệ, xuất kho, điều chuyển giữa
các kho, kiểm kê, điều chỉnh tồn, cùng phân quyền chi tiết theo chức năng và
theo phạm vi kho.


-------------------------------------------------------------------------------
 1. TÍNH NĂNG CHÍNH
-------------------------------------------------------------------------------

Danh mục nền (Master data)
  - Sản phẩm và danh mục sản phẩm (kèm ảnh sản phẩm qua Cloudinary).
  - Đối tác: nhà cung cấp, khách hàng, đơn vị vận chuyển.
  - Kho và ô kệ (bin location) theo toạ độ zone-aisle-rack-bin.

Nhập kho (Inbound)
  - Đơn mua hàng (Purchase Order): tạo, trình duyệt, duyệt/từ chối.
  - Phiếu nhập kho (Goods Receipt): tạo từ đơn mua, hoàn tất và cất hàng.

Tồn kho (Inventory)
  - Tồn kho theo sản phẩm / kho / lô (batch) / ô kệ, kèm hạn sử dụng.
  - Khởi tạo và điều chỉnh tồn-lô.
  - In tem mã vạch (Code128) cho từng lô.
  - Thẻ kho (Kardex) - lịch sử phát sinh (stock movements).

Xuất kho (Outbound)
  - Đơn bán hàng (Sales Order): tạo, phân bổ tồn (allocate), huỷ.
  - Lệnh lấy hàng (Picking List): sinh theo FEFO, gán người lấy, xác nhận /
    báo thiếu từng dòng, hoàn tất.
  - Quét lấy hàng (Picking Scan): giao diện quét mã cho nhân viên.
  - Vận đơn (Shipment): tạo và xuất hàng / giao vận.

Điều chuyển (Transfer)
  - Phiếu điều chuyển giữa hai kho: tạo, duyệt, xuất kho nguồn, nhận kho đích,
    kèm luồng lấy hàng riêng cho điều chuyển.

Kiểm kê (Stocktake) và Điều chỉnh (Adjustment)
  - Phiên kiểm kê, đối chiếu, duyệt chênh lệch.
  - Phiếu điều chỉnh tồn với quy trình duyệt nhiều tầng theo phần trăm lệch.

Quản trị và hệ thống
  - Người dùng, vai trò (role), quyền (permission) - RBAC hạt mịn.
  - Gán quyền tiếp cận kho cho từng người dùng (warehouse scope).
  - Thông báo (notification) thời gian thực qua WebSocket.
  - Nhập / xuất dữ liệu Excel.
  - Báo cáo Xuất - Nhập - Tồn cho bảng điều khiển (Dashboard).


-------------------------------------------------------------------------------
 2. CÔNG NGHỆ SỬ DỤNG
-------------------------------------------------------------------------------

Backend
  - Java 21, Spring Boot 4.0.6 (Maven).
  - Spring Web MVC (REST), Spring Data JPA / Hibernate.
  - Spring Security + JWT (thư viện jjwt 0.11.5), OAuth2 client.
  - Spring WebSocket + STOMP (thông báo thời gian thực).
  - Spring Cache + Caffeine.
  - MySQL (driver mysql-connector-j).
  - Cloudinary (lưu ảnh sản phẩm).
  - Testcontainers (kiểm thử tích hợp trên MySQL thật).

Frontend
  - React 19 + Vite 6.
  - Ant Design 5 (thư viện giao diện) + @ant-design/icons.
  - TanStack React Query (quản lý dữ liệu máy chủ / cache).
  - React Router 7.
  - Axios (gọi API).
  - @stomp/stompjs + sockjs-client (WebSocket).
  - @zxing/browser, @zxing/library (quét mã vạch bằng camera).
  - jsbarcode (sinh mã vạch để in tem).
  - xlsx (nhập / xuất Excel).
  - dayjs (xử lý ngày giờ).
  - ESLint + Vitest + Testing Library + jsdom (lint và test).


-------------------------------------------------------------------------------
 3. KIẾN TRÚC
-------------------------------------------------------------------------------

Backend theo kiến trúc phân tầng kiểu Hexagonal / DDD:

  domain/          - Mô hình nghiệp vụ thuần (model, repository interface,
                     domain service). Không phụ thuộc framework.
  application/     - Use case (điều phối nghiệp vụ), ports, event.
  infrastructure/  - Hiện thực kỹ thuật: JPA entity + mapper + repository impl,
                     security (JWT, scope kho), config, scheduler, tích hợp
                     ngoài (Cloudinary...).
  interfaces/      - Lớp tiếp xúc: REST controller, DTO request/response,
                     mapper, xử lý ngoại lệ.

Frontend tổ chức theo tính năng (feature-based):

  src/
    api/         - Lớp gọi REST (mỗi domain một file *.api.js).
    auth/        - Ngữ cảnh đăng nhập, bảo vệ route.
    components/  - Thành phần dùng chung (bảng, link, nút xuất Excel...).
    constants/   - Hằng số (danh sách permission...).
    features/    - Từng phân hệ: auth, dashboard, products, categories,
                   partners, warehouses, inbound, inventory, outbound,
                   transfer, stocktake, users, roles, permissions,
                   notifications, data-io.
    hooks/       - Hook tra cứu / tiện ích dùng chung.
    routes/      - Khai báo route.


-------------------------------------------------------------------------------
 4. YÊU CẦU MÔI TRƯỜNG
-------------------------------------------------------------------------------

  - JDK 21 trở lên.
  - Maven 3.9+ (hoặc dùng Maven wrapper ./mvnw nếu có).
  - Node.js 18 trở lên và npm.
  - MySQL 8.x đang chạy (dự án hay dùng kèm WAMP).
  - (Tuỳ chọn) Tài khoản Cloudinary nếu cần upload ảnh sản phẩm.
  - (Kiểm thử tích hợp) Docker, cho Testcontainers.


-------------------------------------------------------------------------------
 5. CẤU TRÚC THƯ MỤC
-------------------------------------------------------------------------------

  MWS-System/
    backend/     - Mã nguồn Spring Boot (Maven).
    frontend/    - Mã nguồn React (Vite).
    db/          - Kịch bản cơ sở dữ liệu:
                     demo.sql                  (tạo 31 bảng + seed tối thiểu)
                     seed_permissions_full.sql (catalog quyền + gói vai trò)
    README.md


-------------------------------------------------------------------------------
 6. CÀI ĐẶT VÀ CHẠY (TỪNG BƯỚC)
-------------------------------------------------------------------------------

Backend cấu hình hibernate ddl-auto = validate, tức KHÔNG tự tạo bảng - phải nạp
schema thủ công trước. Làm lần lượt từ số 0.

Bước 0 - Cài công cụ
   Cài JDK 21, Maven, Node.js 18+, MySQL 8. Kiểm tra:
       java -version      # phải là 21
       mvn -v
       node -v
       mysql --version

Bước 1 - Lấy mã nguồn
       git clone https://github.com/Felsl/MWS-System.git
       cd MWS-System

Bước 2 - Tạo database và nạp schema
       mysql -u root -p -e "CREATE DATABASE mws CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
       mysql -u root -p mws < db/demo.sql

Bước 3 - Tạo vai trò (roles)
   File seed quyền tra theo MÃ role, nên phải có role TRƯỚC (xem thêm mục 9).
   Tối thiểu tạo ADMIN (thêm các role vận hành nếu cần):
       INSERT INTO roles (id, code, name) VALUES
         ('ROLE_ADMIN', 'ADMIN', 'Quản trị hệ thống');
       -- tuỳ chọn thêm:
       -- ('ROLE_WHM','WH_MANAGER','Quản lý kho'),
       -- ('ROLE_SK','STOREKEEPER','Thủ kho'),
       -- ('ROLE_PICK','PICKER','Nhân viên lấy hàng'),
       -- ('ROLE_PUR','PURCHASER','Nhân viên mua hàng'),
       -- ('ROLE_SALE','SALESMAN','Nhân viên bán hàng');

Bước 4 - Nạp catalog quyền và gán quyền cho role
       mysql -u root -p mws < db/seed_permissions_full.sql
   Sau bước này role ADMIN nhận TẤT CẢ quyền; các role vận hành nhận gói quyền
   tương ứng (nếu bạn đã tạo ở bước 3).

Bước 5 - Tạo người dùng đăng nhập đầu tiên (repo không kèm sẵn tài khoản)
   Mật khẩu lưu dạng băm BCrypt. Sinh chuỗi băm bằng công cụ bất kỳ (ví dụ trang
   bcrypt-generator.com, hoặc lệnh của thư viện bcrypt), rồi chèn:
       INSERT INTO users (id, username, password, full_name, role_id, status)
       VALUES ('U_ADMIN', 'admin', '<CHUOI_BCRYPT_CUA_BAN>',
               'System Administrator', 'ROLE_ADMIN', 'ACTIVE');
   Lưu ý: cột password phải là chuỗi BCrypt (bắt đầu bằng $2a$/$2b$), KHÔNG phải
   mật khẩu thô.

Bước 6 - Cấu hình và chạy backend
   Ứng dụng đọc cấu hình nhạy cảm qua biến môi trường (xem
   backend/src/main/resources/application.properties). Cần đặt:
       data_src_url        vd: jdbc:mysql://localhost:3306/mws
       data_src_name       tên đăng nhập MySQL
       data_src_password   mật khẩu MySQL
       jwt_secret          chuỗi bí mật ký JWT (đủ dài)
       cloudinary_name     (nếu dùng upload ảnh)
       cloudinary_key
       api_secret          (Cloudinary api secret)
   Chạy:
       cd backend
       mvn spring-boot:run
       # hoặc: mvn clean package && java -jar target/mws-*.jar
   Backend lên ở http://localhost:8080. Cấu hình JWT mặc định: access token 24
   giờ (86400000 ms), refresh token 7 ngày (604800000 ms).

Bước 7 - Chạy frontend
       cd frontend
       npm install
       npm run dev            # chạy dev ở cổng 5173
       npm run dev:mobile     # chạy dev cho phép truy cập từ thiết bị khác (LAN)
   Khi chạy dev, Vite proxy sẵn /api và /ws sang backend http://localhost:8080
   nên không cần bật CORS. Mở http://localhost:5173 và đăng nhập bằng tài khoản
   đã tạo ở Bước 5.

   Build production:
       npm run build          # xuất ra thư mục dist/
       npm run preview        # xem thử bản build
   Khi build production (không qua proxy), đặt biến VITE_API_BASE_URL trỏ thẳng
   tới backend (xem frontend/.env.example).

Kiểm tra nhanh sau khi chạy
   - Đăng nhập được bằng admin.
   - Vào menu "Danh mục gốc" và "Tồn kho" không bị lỗi 403.
   Nếu 403: xem "Lỗi thường gặp" bên dưới.

Lỗi thường gặp
   - Backend báo lỗi schema / validate khi khởi động:
       Chưa nạp db/demo.sql, hoặc nạp vào sai database. Nạp lại ở Bước 2.
   - Đăng nhập báo sai mật khẩu dù nhập đúng:
       Cột password chưa phải BCrypt (đang là mật khẩu thô). Cập nhật lại ở
       Bước 5.
   - Đăng nhập được nhưng mọi màn báo 403 / danh sách kho không tải:
       Role của user chưa có quyền. Nguyên nhân hay gặp nhất: chạy
       seed_permissions_full.sql TRƯỚC khi tạo roles (Bước 3), khiến các lệnh
       gán quyền khớp 0 dòng. Cách sửa: tạo roles rồi chạy lại file seed
       (chạy lại vô hại). Kiểm tra nhanh:
         SELECT COUNT(*) FROM role_permissions;   -- nếu = 0 là đúng bệnh
   - Không kết nối được API khi chạy dev:
       Đảm bảo backend đã chạy ở cổng 8080 (Vite proxy trỏ tới đó).


-------------------------------------------------------------------------------
 7. HƯỚNG DẪN SỬ DỤNG
-------------------------------------------------------------------------------

Quy trình dùng hệ thống đi theo trình tự nghiệp vụ dưới đây. Tên trong ngoặc là
mục trên thanh menu bên trái.

7.1. Thiết lập ban đầu (làm một lần, cần quyền quản trị)
   1) Tạo kho và ô kệ (Danh mục gốc > Kho): khai báo kho, sinh ô kệ theo toạ độ
      zone-aisle-rack-bin.
   2) Tạo danh mục và sản phẩm (Danh mục gốc > Sản phẩm): mã, tên, đơn vị, ảnh.
   3) Tạo đối tác (Danh mục gốc): nhà cung cấp, khách hàng, đơn vị vận chuyển.
   4) Phân quyền (Quản trị):
        - Tạo vai trò và gán quyền cho vai trò (Vai trò / Quyền).
        - Tạo người dùng, gán vai trò.
        - Gán kho cho người dùng nếu muốn giới hạn phạm vi (không gán = thấy
          mọi kho). Xem mục 9 về cơ chế phạm vi kho.

7.2. Nhập kho
   1) Tạo đơn mua hàng (Nhập kho > Đơn mua hàng): chọn nhà cung cấp, thêm dòng
      sản phẩm và số lượng, trình duyệt.
   2) Duyệt đơn mua (người có quyền duyệt): duyệt hoặc từ chối.
   3) Tạo phiếu nhập kho (Nhập kho > Phiếu nhập kho) từ đơn đã duyệt: nhập số
      lô, hạn sử dụng, ô kệ.
   4) Hoàn tất nhập kho: tồn được cộng vào theo lô và ô kệ.

7.3. Tồn kho và tem
   - Xem tồn theo sản phẩm / kho / lô / ô kệ (Tồn kho > Tồn kho & lô).
   - Khởi tạo hoặc điều chỉnh tồn-lô khi cần.
   - In tem mã vạch cho lô (Tồn kho > In tem mã vạch): chọn kho và sản phẩm,
      tick các lô, bấm In. Khi in đặt tỷ lệ 100%, không chọn "Fit to page".
   - Tra lịch sử phát sinh của từng mặt hàng (Tồn kho > Thẻ kho / Kardex).

7.4. Xuất kho
   1) Tạo đơn bán (Xuất kho > Đơn bán hàng): chọn khách hàng, thêm dòng hàng.
   2) Phân bổ tồn (Allocate): hệ thống giữ tồn cho đơn.
   3) Tạo lệnh lấy hàng (Picking) từ đơn đã phân bổ: hệ thống sinh danh sách lấy
      theo FEFO (ưu tiên lô hết hạn trước).
   4) Gán người lấy, rồi lấy hàng:
        - Trang Lệnh lấy hàng: xác nhận từng dòng hoặc báo thiếu.
        - Hoặc Quét lấy hàng: quét mã lệnh và mã lô để xác nhận nhanh.
   5) Hoàn tất lệnh lấy. Sau đó tạo vận đơn (Xuất kho > Vận đơn) và xuất hàng.

7.5. Điều chuyển giữa kho (Phiếu điều chuyển)
   Tạo phiếu điều chuyển từ kho nguồn sang kho đích, duyệt, xuất kho nguồn, lấy
   hàng, rồi nhận ở kho đích.

7.6. Kiểm kê và điều chỉnh (Kiểm kê)
   - Tạo phiên kiểm kê, nhập số đếm thực tế, hệ thống đối chiếu với tồn sổ.
   - Chênh lệch được lập phiếu điều chỉnh và duyệt (quy trình duyệt nhiều tầng
      theo phần trăm lệch).

7.7. Tiện ích
   - Thông báo (biểu tượng chuông): cảnh báo vận hành thời gian thực.
   - Nhập / Xuất Excel (Nhập/Xuất Excel): nhập dữ liệu hàng loạt hoặc xuất báo
      cáo.
   - Tổng quan (Dashboard): báo cáo Xuất - Nhập - Tồn, cảnh báo cận hạn / tồn
      an toàn.

Ghi chú về phạm vi hiển thị: người dùng chỉ được gán một số kho sẽ chỉ thấy dữ
liệu (tồn, lô, phiếu...) thuộc các kho đó; tài khoản quản trị (không giới hạn
kho) thấy toàn bộ.


-------------------------------------------------------------------------------
 8. LỆNH KIỂM THỬ / CHẤT LƯỢNG MÃ
-------------------------------------------------------------------------------

Frontend:
       npm run lint           # ESLint
       npm test               # Vitest (chạy một lần)
       npm run test:watch     # Vitest (theo dõi)

Backend:
       cd backend
       mvn test               # gồm kiểm thử tích hợp qua Testcontainers (cần Docker)


-------------------------------------------------------------------------------
 9. PHÂN QUYỀN (RBAC) VÀ PHẠM VI KHO
-------------------------------------------------------------------------------

Hệ thống dùng hai lớp kiểm soát độc lập:

A. Quyền theo chức năng (permission)
   - Mọi kiểm tra ở backend dựa trên PERMISSION (hasAuthority), không dựa trên
     tên role. Role chỉ là "bó" các permission để gán cho người dùng nhanh.
   - Danh mục quyền đầy đủ nằm ở db/seed_permissions_full.sql, đặt tên theo
     dạng MODULE_HANHDONG, ví dụ: WAREHOUSE_VIEW, INVENTORY_VIEW,
     INVENTORY_ADJUST, INBOUND_APPROVE_PO, OUTBOUND_PICK, TRANSFER_APPROVE...
   - Controller thường đặt @PreAuthorize ở cấp class cho quyền xem
     (ví dụ WarehouseController yêu cầu WAREHOUSE_VIEW cho mọi endpoint), và
     đặt thêm ở cấp method cho quyền ghi. Nghĩa là để dùng một màn hình, role
     phải có ĐỦ quyền xem của module đó.

   Lưu ý quan trọng khi seed:
     - seed_permissions_full.sql phải chạy SAU khi bảng roles đã có dữ liệu.
       Các lệnh gán quyền tra theo mã role (ADMIN, WH_MANAGER, STOREKEEPER,
       PICKER, PURCHASER, SALESMAN); nếu roles chưa tồn tại thì các lệnh gán
       sẽ khớp 0 dòng và người dùng sẽ không có quyền nào (dẫn tới lỗi 403 ở
       mọi màn có kiểm quyền xem).
     - Nếu một role vận hành cần xem được danh sách kho (ví dụ để chọn kho ở
       trang Tồn kho), role đó phải có quyền WAREHOUSE_VIEW.

B. Phạm vi kho (warehouse scope)
   - Bảng user_warehouse_access gán cho mỗi người dùng danh sách kho được phép
     truy cập.
   - Quy ước: nếu người dùng KHÔNG có bản ghi nào trong bảng này thì được coi
     là toàn cục (thấy mọi kho - dành cho quản trị). Nếu có bản ghi thì chỉ
     thấy dữ liệu của các kho được gán.
   - Muốn giới hạn một người dùng vào một số kho: gán kho cho họ. Muốn họ thấy
     tất cả: không cần gán (chỉ cần cấp quyền chức năng).


-------------------------------------------------------------------------------
 10. GIAO DIỆN API VÀ WEBSOCKET
-------------------------------------------------------------------------------

  - REST base: hầu hết endpoint dưới /api/... Một số module dùng tiền tố phiên
    bản /api/v1/... (ví dụ /api/v1/picking-lists), một số không (ví dụ
    /api/sales-orders). Xem cụ thể trong từng file frontend/src/api/*.api.js.
  - Xác thực: đăng nhập tại /api/v1/auth/login trả về JWT; gửi kèm header
    Authorization: Bearer <token> cho các request sau. Có /api/v1/auth/refresh
    để làm mới token.
  - WebSocket: handshake tại /ws (có SockJS fallback). Broker in-memory với
    /topic (broadcast) và /queue (điểm-điểm qua /user); tiền tố gửi lên là /app.


-------------------------------------------------------------------------------
 11. GHI CHÚ
-------------------------------------------------------------------------------

  - Backend không tự tạo bảng (ddl-auto=validate) - luôn nạp db/demo.sql trước
    khi khởi động, nếu không ứng dụng sẽ báo lỗi lệch schema.
  - Repo không kèm sẵn tài khoản đăng nhập; cần tự tạo user + role ADMIN như
    hướng dẫn ở mục 6 (Bước 3 và Bước 5).
  - Mã định danh nội bộ (id) dạng chuỗi ngắn 8 ký tự; các màn hình hiển thị mã
    nghiệp vụ thân thiện (số phiếu, số đơn) thay cho id khi có thể.
  - Khi in tem mã vạch, đặt tỷ lệ in 100% (không chọn "Fit to page") để mã vạch
    giữ đúng kích thước và quét được.