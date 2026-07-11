import * as XLSX from 'xlsx'

// Tải file .xlsx mẫu: chỉ có 1 hàng tiêu đề = tên các trường (giống form tạo).
export function downloadTemplate(headers, filename) {
  const ws = XLSX.utils.aoa_to_sheet([headers])
  const wb = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(wb, ws, 'Template')
  XLSX.writeFile(wb, filename)
}

// Xuất mảng object ra .xlsx (mỗi key = 1 cột). Dùng cho export danh sách.
export function exportRows(rows, filename) {
  const ws = XLSX.utils.json_to_sheet(rows)
  const wb = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(wb, ws, 'Data')
  XLSX.writeFile(wb, filename)
}

// Đọc file .xlsx/.csv người dùng upload -> mảng object (key theo hàng tiêu đề).
export async function parseWorkbook(file) {
  const buf = await file.arrayBuffer()
  const wb = XLSX.read(buf, { type: 'array' })
  const ws = wb.Sheets[wb.SheetNames[0]]
  return XLSX.utils.sheet_to_json(ws, { defval: '' })
}
