-- =========================================================================
-- ĐỒ ÁN LUẬN VĂN TỐT NGHIỆP: HỆ THỐNG QUẢN LÝ KHO (WMS) ĐA CHI NHÁNH
-- FILE: demo_updated.sql (Đã tối ưu hóa Scope theo thống nhất)
-- Tương thích: MySQL 8.x / Engine: InnoDB
-- =========================================================================

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS notifications;
DROP TABLE IF EXISTS stock_movements;
DROP TABLE IF EXISTS adjustment_voucher_details;
DROP TABLE IF EXISTS adjustment_vouchers;
DROP TABLE IF EXISTS stocktake_details;
DROP TABLE IF EXISTS stocktake_sessions;
DROP TABLE IF EXISTS transfer_order_details;
DROP TABLE IF EXISTS transfer_orders;
DROP TABLE IF EXISTS return_order_details;
DROP TABLE IF EXISTS return_orders;
DROP TABLE IF EXISTS shipments;
DROP TABLE IF EXISTS picking_list_details;
DROP TABLE IF EXISTS picking_lists;
DROP TABLE IF EXISTS sales_order_details;
DROP TABLE IF EXISTS sales_orders;
DROP TABLE IF EXISTS goods_receipt_details;
DROP TABLE IF EXISTS goods_receipts;
DROP TABLE IF EXISTS purchase_order_details;
DROP TABLE IF EXISTS purchase_orders;
DROP TABLE IF EXISTS inventory_batches;
DROP TABLE IF EXISTS inventory;
DROP TABLE IF EXISTS user_warehouse_access;
DROP TABLE IF EXISTS user_direct_permissions;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS role_permissions;
DROP TABLE IF EXISTS roles;
DROP TABLE IF EXISTS permissions;
DROP TABLE IF EXISTS carriers;
DROP TABLE IF EXISTS customers;
DROP TABLE IF EXISTS suppliers;
DROP TABLE IF EXISTS bin_locations;
DROP TABLE IF EXISTS warehouses;
DROP TABLE IF EXISTS product_price_history;
DROP TABLE IF EXISTS product_uom_conversions;
DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS product_categories;
DROP TABLE IF EXISTS unit_of_measures;
DROP TABLE IF EXISTS number_sequences;
DROP TABLE IF EXISTS system_settings;
SET FOREIGN_KEY_CHECKS = 1;

-- =========================================================================
-- PHÂN HỆ 1: DANH MỤC GỐC & CẤU HÌNH (MASTER DATA & SETTINGS)
-- =========================================================================

-- Đơn vị tính bây giờ quản lý trực tiếp bằng Java Enum ở tầng Application.

CREATE TABLE product_categories (
    id VARCHAR(20) PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    deleted_at TIMESTAMP NULL DEFAULT NULL
) ENGINE=InnoDB;

CREATE TABLE products (
    id VARCHAR(20) PRIMARY KEY,
    category_id VARCHAR(20),
    sku VARCHAR(50) UNIQUE NOT NULL,
    barcode VARCHAR(50)  NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    cost_price DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    -- [ĐIỂM 2] Unit chuyển sang VARCHAR để map trực tiếp với Java Enum (PCS, BOX, KG, BAG...) ở Spring Boot
    unit VARCHAR(20) NOT NULL, 
    safety_stock INT NOT NULL DEFAULT 10,
    weight DECIMAL(10,2) NULL,
    volume DECIMAL(10,2) NULL,
    hazardous_flag BOOLEAN DEFAULT FALSE,
    version INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL DEFAULT NULL,
    FOREIGN KEY (category_id) REFERENCES product_categories(id) ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE TABLE warehouses (
    id VARCHAR(20) PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    address VARCHAR(255) NOT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL DEFAULT NULL
) ENGINE=InnoDB;

CREATE TABLE bin_locations (
    id VARCHAR(20) PRIMARY KEY,
    warehouse_id VARCHAR(20) NOT NULL,
    zone VARCHAR(50) NOT NULL,
    aisle VARCHAR(50) NOT NULL,
    rack VARCHAR(50) NOT NULL,
    bin VARCHAR(50) NOT NULL,
    UNIQUE KEY uq_location (warehouse_id, zone, aisle, rack, bin),
    FOREIGN KEY (warehouse_id) REFERENCES warehouses(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE suppliers (
    id VARCHAR(20) PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    contact_name VARCHAR(100),
    phone VARCHAR(20),
    email VARCHAR(100),
    address TEXT,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    deleted_at TIMESTAMP NULL DEFAULT NULL
) ENGINE=InnoDB;

CREATE TABLE customers (
    id VARCHAR(20) PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    tax_code VARCHAR(50),
    phone VARCHAR(20),
    email VARCHAR(100),
    address TEXT,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL DEFAULT NULL
) ENGINE=InnoDB;



-- =========================================================================
-- PHÂN HỆ 2: TÀI KHOẢN & PHÂN QUYỀN HỖN HỢP (USER, ROLE & PERMISSIONS)
-- =========================================================================

CREATE TABLE permissions (
    id VARCHAR(20) PRIMARY KEY,
    code VARCHAR(100) UNIQUE NOT NULL, -- e.g., 'INBOUND_CREATE_PO'
    name VARCHAR(150) NOT NULL,
    module VARCHAR(50) NOT NULL
) ENGINE=InnoDB;

CREATE TABLE roles (
    id VARCHAR(20) PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL, -- e.g., 'STOREKEEPER', 'PICKER'
    name VARCHAR(100) NOT NULL,
    description TEXT
) ENGINE=InnoDB;

CREATE TABLE role_permissions (
    role_id VARCHAR(20) NOT NULL,
    permission_id VARCHAR(20) NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE users (
    id VARCHAR(20) PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL, -- BCrypt Hash
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NULL,
    phone VARCHAR(20) NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    role_id VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL DEFAULT NULL,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE user_warehouse_access (
    user_id VARCHAR(20) NOT NULL,
    warehouse_id VARCHAR(20) NOT NULL,
    PRIMARY KEY (user_id, warehouse_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (warehouse_id) REFERENCES warehouses(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- =========================================================================
-- PHÂN HỆ 3: QUẢN LÝ TỒN KHO VẬT LÝ & LÔ SẢN XUẤT (INVENTORY & BATCHES)
-- =========================================================================

CREATE TABLE inventory (
    product_id VARCHAR(20) NOT NULL,
    warehouse_id VARCHAR(20) NOT NULL,
    quantity INT NOT NULL DEFAULT 0,
    reserved_quantity INT NOT NULL DEFAULT 0, -- Giữ chỗ ảo khi SO/Transfer ở trạng thái PENDING
    version INT NOT NULL DEFAULT 0, -- Thao tác Optimistic Locking bảo vệ trừ tồn kho đồng thời
    PRIMARY KEY (product_id, warehouse_id),
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    FOREIGN KEY (warehouse_id) REFERENCES warehouses(id) ON DELETE CASCADE,
    CONSTRAINT chk_inv_qty CHECK (quantity >= 0),
    CONSTRAINT chk_inv_reserved CHECK (reserved_quantity >= 0),
    CONSTRAINT chk_inv_logical CHECK (quantity >= reserved_quantity)
) ENGINE=InnoDB;

CREATE TABLE inventory_batches (
    id VARCHAR(20) PRIMARY KEY,
    product_id VARCHAR(20) NOT NULL,
    warehouse_id VARCHAR(20) NOT NULL,
    bin_location_id VARCHAR(20) NOT NULL,
    batch_number VARCHAR(50) NOT NULL, -- Sinh mã chuỗi từ SequenceGenerator ở Spring Boot
    quantity INT NOT NULL DEFAULT 0,
    expiry_date DATE NULL, -- Tiêu chí chạy lọc thuật toán FEFO
    manufactured_date DATE NULL,
    status VARCHAR(30) DEFAULT 'ACTIVE', -- ACTIVE, HOLD, EXPIRED
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(id),
    FOREIGN KEY (warehouse_id) REFERENCES warehouses(id),
    FOREIGN KEY (bin_location_id) REFERENCES bin_locations(id),
    CONSTRAINT chk_batch_qty CHECK (quantity >= 0),
    UNIQUE KEY uq_product_batch_location (product_id, warehouse_id, bin_location_id, batch_number)
) ENGINE=InnoDB;

-- =========================================================================
-- PHÂN HỆ 4: LUỒNG NHẬP KHO (INBOUND LOGISTICS)
-- =========================================================================

CREATE TABLE purchase_orders (
    id VARCHAR(20) PRIMARY KEY,
    po_number VARCHAR(50) UNIQUE NOT NULL,
    supplier_id VARCHAR(20) NOT NULL,
    warehouse_id VARCHAR(20) NOT NULL,
    status VARCHAR(30) NOT NULL, -- DRAFT -> PENDING_REVIEW -> PENDING_APPROVAL -> APPROVED -> (ORDERED, CLOSED)/(REJECTED/CANCELLED)
    expected_date DATE NULL,
    created_by VARCHAR(50) NOT NULL,
    approved_by VARCHAR(50) NULL, -- [ĐÃ THÊM] Người duyệt đơn mua
    approved_at TIMESTAMP NULL DEFAULT NULL, -- [ĐÃ THÊM] Ngày duyệt đơn mua
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (supplier_id) REFERENCES suppliers(id),
    FOREIGN KEY (warehouse_id) REFERENCES warehouses(id)
) ENGINE=InnoDB;

CREATE TABLE purchase_order_details (
    id VARCHAR(20) PRIMARY KEY,
    po_id VARCHAR(20) NOT NULL,
    product_id VARCHAR(20) NOT NULL,
    quantity_ordered INT NOT NULL,
    quantity_received INT NOT NULL DEFAULT 0,
    unit_price DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    FOREIGN KEY (po_id) REFERENCES purchase_orders(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT chk_po_ordered CHECK (quantity_ordered > 0)
) ENGINE=InnoDB;

CREATE TABLE goods_receipts (
    id VARCHAR(20) PRIMARY KEY,
    grn_number VARCHAR(50) UNIQUE NOT NULL, -- Sinh mã chuỗi nghiệp vụ hiển thị
    po_id VARCHAR(20) NULL, -- Cho phép NULL nếu nhập ngoài luồng mua hàng
    warehouse_id VARCHAR(20) NOT NULL,
    status VARCHAR(30) NOT NULL, -- PENDING, COMPLETED
    received_by VARCHAR(50) NOT NULL,
    received_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    note TEXT,
    FOREIGN KEY (po_id) REFERENCES purchase_orders(id) ON DELETE SET NULL,
    FOREIGN KEY (warehouse_id) REFERENCES warehouses(id)
) ENGINE=InnoDB;

CREATE TABLE goods_receipt_details (
    id VARCHAR(20) PRIMARY KEY,
    grn_id VARCHAR(20) NOT NULL,
    product_id VARCHAR(20) NOT NULL,
    po_detail_id VARCHAR(20) NULL,
    -- [ĐIỂM 4] Đã gộp quantity_received, quantity_accepted, quantity_rejected làm 1 trường duy nhất để xóa bỏ "dead column"
    quantity INT NOT NULL, 
    batch_number VARCHAR(50) NULL,
    expiry_date DATE NULL,
    bin_location_id VARCHAR(20) NOT NULL, -- Putaway chỉ định ô kệ cất hàng trực tiếp
    FOREIGN KEY (grn_id) REFERENCES goods_receipts(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id),
    FOREIGN KEY (po_detail_id) REFERENCES purchase_order_details(id) ON DELETE SET NULL,
    FOREIGN KEY (bin_location_id) REFERENCES bin_locations(id),
    CONSTRAINT chk_grn_qty CHECK (quantity > 0)
) ENGINE=InnoDB;

-- =========================================================================
-- PHÂN HỆ 5: LUỒNG XUẤT KHO & VẬN CHUYỂN (OUTBOUND & LOGISTICS)
-- =========================================================================

CREATE TABLE sales_orders (
    id VARCHAR(20) PRIMARY KEY,
    so_number VARCHAR(50) UNIQUE NOT NULL, -- Sinh mã chuỗi nghiệp vụ hiển thị
    warehouse_id VARCHAR(20) NOT NULL,
    customer_id VARCHAR(20) NOT NULL,
    discount_amount DECIMAL(15,2) DEFAULT 0.00,
    status VARCHAR(30) NOT NULL, -- DRAFT, ALLOCATED, PICKING, SHIPPED, CANCELLED
    priority INT DEFAULT 0,
    required_date DATE NULL,
    created_by VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (warehouse_id) REFERENCES warehouses(id),
    FOREIGN KEY (customer_id) REFERENCES customers(id)
) ENGINE=InnoDB;

CREATE TABLE sales_order_details (
    id VARCHAR(20) PRIMARY KEY,
    so_id VARCHAR(20) NOT NULL,
    product_id VARCHAR(20) NOT NULL,
    quantity_ordered INT NOT NULL,
    quantity_picked INT NOT NULL DEFAULT 0,
    unit_price DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    discount_percent DECIMAL(5,2) DEFAULT 0.00,
    FOREIGN KEY (so_id) REFERENCES sales_orders(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT chk_so_ordered CHECK (quantity_ordered > 0)
) ENGINE=InnoDB;

CREATE TABLE picking_lists (
    id VARCHAR(20) PRIMARY KEY,
    so_id VARCHAR(20) NOT NULL,
    assigned_to VARCHAR(20) NULL, -- Tài khoản công nhân nhận lệnh gom hàng
    status VARCHAR(30) NOT NULL, -- PENDING, PICKING, COMPLETED
    started_at TIMESTAMP NULL DEFAULT NULL,
    completed_at TIMESTAMP NULL DEFAULT NULL,
    FOREIGN KEY (so_id) REFERENCES sales_orders(id) ON DELETE CASCADE,
    FOREIGN KEY (assigned_to) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE TABLE picking_list_details (
    id VARCHAR(20) PRIMARY KEY,
    picking_list_id VARCHAR(20) NOT NULL,
    product_id VARCHAR(20) NOT NULL,
    batch_id VARCHAR(20) NOT NULL, -- Đề xuất lô xuất tối ưu theo thuật toán FEFO
    actual_batch_id VARCHAR(20) NULL, -- Đối chiếu thực tế quét mã vạch bằng PDA
    bin_location_id VARCHAR(20) NOT NULL, -- Vị trí ô kệ công nhân di chuyển tới lấy hàng
    quantity_to_pick INT NOT NULL,
    quantity_picked INT NOT NULL DEFAULT 0,
    is_confirmed BOOLEAN DEFAULT FALSE,
    confirmed_by VARCHAR(50) NULL,
    confirmed_at TIMESTAMP NULL DEFAULT NULL,
    FOREIGN KEY (picking_list_id) REFERENCES picking_lists(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id),
    FOREIGN KEY (batch_id) REFERENCES inventory_batches(id),
    FOREIGN KEY (actual_batch_id) REFERENCES inventory_batches(id),
    FOREIGN KEY (bin_location_id) REFERENCES bin_locations(id),
    CONSTRAINT chk_qty_to_pick CHECK (quantity_to_pick > 0)
) ENGINE=InnoDB;


-- =========================================================================
-- PHÂN HỆ 6: ĐIỀU CHUYỂN NỘI BỘ (INTERNAL TRANSFER) - Điểm nhấn đa chi nhánh
-- =========================================================================
CREATE TABLE transfer_orders (
    id VARCHAR(20) PRIMARY KEY,
    from_warehouse_id VARCHAR(20) NOT NULL,
    to_warehouse_id VARCHAR(20) NOT NULL,
    transfer_number VARCHAR(50) UNIQUE NOT NULL,
    status VARCHAR(30) NOT NULL, 
    created_by VARCHAR(50) NOT NULL,
    approved_by VARCHAR(50) NULL,
    approved_at TIMESTAMP NULL DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (from_warehouse_id) REFERENCES warehouses(id),
    FOREIGN KEY (to_warehouse_id) REFERENCES warehouses(id),
    -- Ràng buộc chặn đứng lỗi logic chuyển trùng kho
    CONSTRAINT chk_diff_warehouse CHECK (from_warehouse_id <> to_warehouse_id)
) ENGINE=InnoDB;

-- [ĐIỂM 3] Đã GIỮ LẠI bảng carriers để phục vụ luồng Shipments tích hợp cho cả SO và Transfer Orders
CREATE TABLE carriers (
    id VARCHAR(20) PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    shipping_fee_rule TEXT, -- Lưu cấu hình JSON định mức tính phí ship
    status VARCHAR(20) DEFAULT 'ACTIVE'
) ENGINE=InnoDB;
-- [ĐIỂM 3] Đã giữ lại và cấu hình bảng Shipments làm trung tâm quản lý giao vận 
-- Kiện hàng xuất đi chặng ngoài cho cả 2 chứng từ gốc: SALES_ORDER và TRANSFER_ORDER
CREATE TABLE shipments (
    id VARCHAR(20) PRIMARY KEY,
    shipment_number VARCHAR(50) UNIQUE NOT NULL,
    sales_order_id VARCHAR(20) NULL,      -- FK tường minh 1
    transfer_order_id VARCHAR(20) NULL,   -- FK tường minh 2
    carrier_id VARCHAR(20) NULL,
    tracking_number VARCHAR(100) NULL,
    status VARCHAR(30) NOT NULL,          -- PACKED, HANDED_OVER, SHIPPING, DELIVERED
    shipped_at TIMESTAMP NULL DEFAULT NULL,
    delivered_at TIMESTAMP NULL DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (carrier_id) REFERENCES carriers(id) ON DELETE SET NULL,
    FOREIGN KEY (sales_order_id) REFERENCES sales_orders(id) ON DELETE CASCADE,
    FOREIGN KEY (transfer_order_id) REFERENCES transfer_orders(id) ON DELETE CASCADE,
    -- Ràng buộc: Bắt buộc phải trỏ vào 1 trong 2 bảng, không được để trống cả hai và không được chọn cả hai
    CONSTRAINT chk_shipment_source CHECK (
        (sales_order_id IS NOT NULL AND transfer_order_id IS NULL) OR 
        (sales_order_id IS NULL AND transfer_order_id IS NOT NULL)
    )
) ENGINE=InnoDB;

CREATE TABLE transfer_order_details (
    id VARCHAR(20) PRIMARY KEY,
    transfer_order_id VARCHAR(20) NOT NULL,
    product_id VARCHAR(20) NOT NULL,
    batch_id VARCHAR(20) NULL, -- Bốc chính xác lô nào đi từ kho nguồn
    quantity INT NOT NULL, -- Số lượng bốc đi tại kho nguồn
    quantity_received INT DEFAULT 0, -- Đối soát số lượng thực nhận khi xe cập bến kho đích
    from_bin_location_id VARCHAR(20) NOT NULL, -- Lấy hàng ra tại ô kệ này của kho nguồn
    bin_location_id VARCHAR(20) NULL, -- Cất hàng vào ô kệ này tại kho đích khi hoàn tất
    FOREIGN KEY (transfer_order_id) REFERENCES transfer_orders(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id),
    FOREIGN KEY (batch_id) REFERENCES inventory_batches(id),
    FOREIGN KEY (from_bin_location_id) REFERENCES bin_locations(id),
    FOREIGN KEY (bin_location_id) REFERENCES bin_locations(id),
    CONSTRAINT chk_trans_qty CHECK (quantity > 0)
) ENGINE=InnoDB;


-- =========================================================================
-- PHÂN HỆ 7: KIỂM KÊ KHO & ĐIỀU CHỈNH ĐỘC LẬP (STOCKTAKE & ADJUSTMENT)
-- =========================================================================

CREATE TABLE stocktake_sessions (
    id VARCHAR(20) PRIMARY KEY,
    warehouse_id VARCHAR(20) NOT NULL,
    status VARCHAR(30) NOT NULL, -- OPEN, FREEZED, ADJUSTED
    freeze_started_at TIMESTAMP NULL DEFAULT NULL,
    freeze_ended_at TIMESTAMP NULL DEFAULT NULL,
    created_by VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (warehouse_id) REFERENCES warehouses(id)
) ENGINE=InnoDB;

-- 1. Cập nhật bảng kiểm kê chi tiết (Thêm batch_id)
CREATE TABLE stocktake_details (
    id VARCHAR(20) PRIMARY KEY,
    session_id VARCHAR(20) NOT NULL,
    product_id VARCHAR(20) NOT NULL,
    bin_location_id VARCHAR(20) NOT NULL,
    batch_id VARCHAR(20) NOT NULL, 
    system_quantity INT NOT NULL,
    counted_quantity INT NULL DEFAULT NULL,
    difference INT NULL DEFAULT NULL, 
    adjustment_reason VARCHAR(100) NULL,
    counted_by VARCHAR(20) NULL,
    counted_at TIMESTAMP NULL DEFAULT NULL,
    approved_by VARCHAR(20) NULL,
    approved_at TIMESTAMP NULL DEFAULT NULL,
    FOREIGN KEY (session_id) REFERENCES stocktake_sessions(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id),
    FOREIGN KEY (bin_location_id) REFERENCES bin_locations(id),
    FOREIGN KEY (batch_id) REFERENCES inventory_batches(id), -- [ĐÃ THÊM FK]
    FOREIGN KEY (counted_by) REFERENCES users(id),
    FOREIGN KEY (approved_by) REFERENCES users(id),
    -- Đảm bảo tính duy nhất: Trong 1 phiên, 1 vị trí, 1 sản phẩm của 1 lô chỉ xuất hiện 1 dòng để đối chiếu
    UNIQUE KEY uq_stocktake_item (session_id, bin_location_id, product_id, batch_id)
) ENGINE=InnoDB;

CREATE TABLE adjustment_vouchers (
    id VARCHAR(20) PRIMARY KEY,
    voucher_number VARCHAR(50) UNIQUE NOT NULL,
    warehouse_id VARCHAR(20) NOT NULL,
    session_id VARCHAR(20) NULL, -- [ĐÃ THÊM] Liên kết trực tiếp với phiên kiểm kê gốc
    reason VARCHAR(100) NOT NULL,
    status VARCHAR(20) DEFAULT 'DRAFT',
    created_by VARCHAR(20) NULL,
    approved_by VARCHAR(20) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (warehouse_id) REFERENCES warehouses(id),
    FOREIGN KEY (session_id) REFERENCES stocktake_sessions(id) ON DELETE SET NULL, -- [ĐÃ THÊM FK]
    FOREIGN KEY (created_by) REFERENCES users(id),
    FOREIGN KEY (approved_by) REFERENCES users(id)
) ENGINE=InnoDB;
CREATE TABLE adjustment_voucher_details (
    id VARCHAR(20) PRIMARY KEY,
    voucher_id VARCHAR(20) NOT NULL,
    product_id VARCHAR(20) NOT NULL,
    batch_id VARCHAR(20) NULL,
    bin_location_id VARCHAR(20) NOT NULL,
    quantity_change INT NOT NULL, 
    before_quantity INT NOT NULL,
    after_quantity INT NOT NULL,
    stocktake_detail_id VARCHAR(20) NULL, 
    
    FOREIGN KEY (voucher_id) REFERENCES adjustment_vouchers(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id),
    FOREIGN KEY (batch_id) REFERENCES inventory_batches(id) ON DELETE SET NULL,
    FOREIGN KEY (bin_location_id) REFERENCES bin_locations(id),
    FOREIGN KEY (stocktake_detail_id) REFERENCES stocktake_details(id) ON DELETE SET NULL
) ENGINE=InnoDB;
-- =========================================================================
-- PHÂN HỆ 8: BIẾN ĐỘNG TỒN KHO & CẢNH BÁO (AUDIT TRAILS & NOTIFICATIONS)
-- =========================================================================

CREATE TABLE stock_movements (
    id VARCHAR(20) PRIMARY KEY,
    product_id VARCHAR(20) NOT NULL,
    warehouse_id VARCHAR(20) NOT NULL,
    batch_id VARCHAR(20) NULL,
    movement_type VARCHAR(30) NOT NULL, -- INBOUND, OUTBOUND, INTERNAL_TRANSFER, ADJUST
    quantity_change INT NOT NULL,
    quantity_before INT NOT NULL,
    quantity_after INT NOT NULL,
    reference_type VARCHAR(30) NULL, -- 'GOODS_RECEIPT', 'SALES_ORDER', 'TRANSFER_ORDER'
    reference_id VARCHAR(20) NULL, -- ID của chứng từ gốc để truy vết ngược nguồn gốc biến động
    note TEXT,
    created_by VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(id),
    FOREIGN KEY (warehouse_id) REFERENCES warehouses(id),
    FOREIGN KEY (batch_id) REFERENCES inventory_batches(id)
) ENGINE=InnoDB;

CREATE TABLE notifications (
    id VARCHAR(20) PRIMARY KEY,
    user_id VARCHAR(20) NULL, -- NULL nghĩa là bắn thông báo chung cho cả kho
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(50) NOT NULL, -- ALERT, INFO, WARNING
    reference_type VARCHAR(30) NULL,
    reference_id VARCHAR(20) NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- =========================================================================
-- PHÂN HỆ 9: CHỈ MỤC TỐI ƯU HÓA TRUY VẤN TRÊN MySQL (INDEXES)
-- =========================================================================

-- Chỉ mục kết hợp (Composite Indexes) thay thế cho Partial Indexes, tối ưu hóa tìm kiếm hàng hóa/đối tác còn hoạt động
CREATE INDEX idx_products_active ON products(sku, barcode, deleted_at);
CREATE INDEX idx_warehouses_active ON warehouses(code, deleted_at);
CREATE INDEX idx_customers_active ON customers(code, phone, deleted_at);
CREATE INDEX idx_suppliers_active ON suppliers(code, deleted_at);
CREATE INDEX idx_users_active ON users(username, deleted_at);

-- Chỉ mục thẻ kho hỗ trợ làm Dashboard phân tích tốc độ luân chuyển hàng hóa (Báo cáo xuất nhập tồn) nhanh nhất
CREATE INDEX idx_movements_prod_wh_date ON stock_movements(product_id, warehouse_id, created_at);
CREATE INDEX idx_movements_reference ON stock_movements(reference_type, reference_id);

-- Chỉ mục hỗ trợ thuật toán lọc hàng cận hạn sử dụng (FEFO) trên ô kệ kho của tầng Backend
CREATE INDEX idx_batches_lookup ON inventory_batches(product_id, warehouse_id, expiry_date, status);
CREATE INDEX idx_movements_date ON stock_movements(created_at);
CREATE INDEX idx_notifications_user_read ON notifications(user_id, is_read);