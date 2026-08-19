-- ============================================================================
-- Backfill NSX + HSD từ goods_receipt_details sang inventory_batches.
--
-- Bối cảnh: trước bản vá này GRN service truyền manufacturedDate=null xuống lô
-- (comment cũ: "goods_receipt_details has no manufactured_date column"). HSD
-- thì có truyền, nhưng vẫn nên đồng bộ lại cho khớp — phòng trường hợp lô
-- được sửa tay hoặc dữ liệu lệch do bug cũ.
--
-- Nguyên tắc join lô ↔ dòng phiếu: khớp trên bộ khóa nhận diện lô đầy đủ
--   (product_id, warehouse_id, bin_location_id, batch_number)
-- Không join theo supplier_id vì lô cũ có thể chưa gắn supplier (nullable ở
-- inventory_batches — comment ở entity nói rõ), tránh loại nhầm.
-- warehouse_id của phiếu lấy từ goods_receipts (dòng chi tiết không có).
--
-- Chỉ ghi khi lô đang NULL — bảo vệ dữ liệu đã có (kể cả do người dùng sửa
-- tay sau này). Nếu muốn OVERWRITE toàn bộ, đổi 2 điều kiện "b.xxx IS NULL"
-- ở subquery WHERE và bỏ COALESCE ở UPDATE.
--
-- Chống mập mờ khi 1 lô match nhiều dòng phiếu (cùng số lô nhập nhiều lần
-- vào cùng ô kệ): dùng MAX() — lấy ngày muộn hơn. Cả 2 chỉ số ngày thường
-- giống nhau giữa các lần nhập cùng lô nên chọn MAX/MIN đều OK; MAX an toàn
-- hơn khi có sai sót ngày sớm ở lần nhập trước.
--
-- CHẠY TRONG TRANSACTION. Backup trước cho chắc.
-- ============================================================================

START TRANSACTION;

UPDATE inventory_batches b
JOIN (
    SELECT
        grd.product_id,
        gr.warehouse_id,
        grd.bin_location_id,
        grd.batch_number,
        MAX(grd.manufactured_date) AS manufactured_date,
        MAX(grd.expiry_date)       AS expiry_date
    FROM goods_receipt_details grd
    JOIN goods_receipts gr ON gr.id = grd.grn_id
    WHERE grd.batch_number IS NOT NULL
      AND (grd.manufactured_date IS NOT NULL OR grd.expiry_date IS NOT NULL)
    GROUP BY grd.product_id, gr.warehouse_id, grd.bin_location_id, grd.batch_number
) src
  ON  src.product_id      = b.product_id
  AND src.warehouse_id    = b.warehouse_id
  AND src.bin_location_id = b.bin_location_id
  AND src.batch_number    = b.batch_number
SET
    b.manufactured_date = COALESCE(b.manufactured_date, src.manufactured_date),
    b.expiry_date       = COALESCE(b.expiry_date,       src.expiry_date)
WHERE
    (b.manufactured_date IS NULL AND src.manufactured_date IS NOT NULL)
 OR (b.expiry_date       IS NULL AND src.expiry_date       IS NOT NULL);

-- Kiểm tra trước khi commit: bao nhiêu lô đã được điền
SELECT
    SUM(manufactured_date IS NOT NULL) AS batches_with_nsx,
    SUM(expiry_date       IS NOT NULL) AS batches_with_hsd,
    COUNT(*)                           AS total_batches
FROM inventory_batches;

COMMIT;
