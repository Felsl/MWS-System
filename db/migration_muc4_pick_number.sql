-- =====================================================================
-- [MỤC 4] Thêm mã nghiệp vụ pick_number cho bảng picking_lists.
-- Chạy MỘT LẦN trên CSDL đang có dữ liệu (demo.sql đã có sẵn cột này cho
-- CSDL tạo mới). Cột để NULLABLE + UNIQUE:
--   - NULLABLE: các lệnh gom hàng cũ chưa có mã sẽ không vi phạm ràng buộc.
--   - Lệnh tạo MỚI luôn được backend sinh mã (PK-XXXXXXXX).
-- (grn_number đã tồn tại từ trước, KHÔNG cần thêm.)
-- =====================================================================

ALTER TABLE picking_lists
    ADD COLUMN pick_number VARCHAR(50) NULL UNIQUE AFTER id;

-- (Tuỳ chọn) Cấp mã cho các lệnh cũ để không còn ô trống trên giao diện.
-- MySQL 8+; bỏ qua nếu không cần.
-- UPDATE picking_lists
--     SET pick_number = CONCAT('PK-', UPPER(SUBSTRING(REPLACE(UUID(),'-',''), 1, 8)))
--     WHERE pick_number IS NULL;
