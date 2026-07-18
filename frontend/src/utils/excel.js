/**
 * Bọc thư viện `xlsx` bằng dynamic import.
 *
 * Lý do: xlsx nặng ~900KB (chưa gzip) nhưng chỉ 2 luồng dùng tới — nút "Xuất
 * Excel" ở các trang danh sách và trang Nhập/Xuất Excel. Nếu `import * as XLSX`
 * ở đầu file thì Rollup nhét thẳng nó vào bundle chính, và MỌI người dùng phải
 * tải 900KB đó ngay khi đăng nhập, kể cả người không bao giờ bấm xuất Excel.
 *
 * Vì vậy mọi hàm ở đây đều async. Trình duyệt chỉ tải chunk xlsx ở lần bấm đầu
 * tiên; các lần sau dùng lại chunk đã cache trong bộ nhớ.
 */

// Cache promise để nhiều lần gọi song song không tạo nhiều request.
let xlsxPromise = null
function loadXLSX() {
  if (!xlsxPromise) xlsxPromise = import('xlsx')
  return xlsxPromise
}

/** Nạp sẵn xlsx (gọi khi rê chuột vào nút xuất) để lúc bấm là có ngay. */
export function preloadXLSX() { loadXLSX() }

// Tải file .xlsx mẫu: chỉ có 1 hàng tiêu đề = tên các trường (giống form tạo).
export async function downloadTemplate(headers, filename) {
  const XLSX = await loadXLSX()
  const ws = XLSX.utils.aoa_to_sheet([headers])
  const wb = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(wb, ws, 'Template')
  XLSX.writeFile(wb, filename)
}

// Xuất mảng object ra .xlsx (mỗi key = 1 cột). Dùng cho export danh sách.
export async function exportRows(rows, filename) {
  const XLSX = await loadXLSX()
  const ws = XLSX.utils.json_to_sheet(rows)
  const wb = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(wb, ws, 'Data')
  XLSX.writeFile(wb, filename)
}

// Đọc file .xlsx/.csv người dùng upload -> mảng object (key theo hàng tiêu đề).
export async function parseWorkbook(file) {
  const XLSX = await loadXLSX()
  const buf = await file.arrayBuffer()
  const wb = XLSX.read(buf, { type: 'array' })
  const ws = wb.Sheets[wb.SheetNames[0]]
  return XLSX.utils.sheet_to_json(ws, { defval: '' })
}
