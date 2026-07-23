/**
 * IN TEM QUA IFRAME SẠCH.
 *
 * Vì sao không dùng window.print() trên chính trang:
 * cách cũ đặt #barcode-print-sheet {position:absolute; width:100%} rồi ẩn phần
 * còn lại bằng visibility:hidden. Nhưng `visibility:hidden` VẪN GIỮ CHỖ trong
 * layout, và antd `.ant-card` có `position:relative` nên khung tham chiếu của
 * position:absolute là CÁI CARD chứ không phải tờ giấy. Kết quả: bề rộng khả
 * dụng chỉ còn ~100mm, `.bl-preview {max-width:100%}` ép 194mm xuống ~100mm =>
 * toàn bộ tem (và mã vạch bên trong) co lại ~50%, X-dimension rơi xuống dưới
 * ngưỡng đọc được của máy quét.
 *
 * In qua iframe: tài liệu trắng, chỉ có CSS tem => hình học đúng mm tuyệt đối,
 * không CSS nào của app chen vào, và tem chảy trang bình thường nên NHIỀU TRANG
 * vẫn đúng (position:absolute/fixed thì không).
 */

/** Hình học tem — dùng chung cho preview trên màn hình và cho bản in. */
export const SHEET_CSS = `
.bl-preview { background: #fff; width: 194mm; max-width: 100%; margin: 0 auto; }
.bl-sheet { display: grid; grid-template-columns: repeat(2, 1fr); gap: 4mm; }
.bl-label {
  border: 1px solid #d9d9d9; border-radius: 4px; padding: 2.5mm 3mm;
  height: 38mm; display: flex; flex-direction: column; justify-content: space-between;
  overflow: hidden; break-inside: avoid; page-break-inside: avoid;
}
.bl-label .bl-code { font-weight: 700; font-size: 11pt; line-height: 1.15;
  white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.bl-label .bl-name { font-size: 8pt; color: #333; line-height: 1.1;
  display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
/* Khung mã vạch 16mm + tem 2 cột: đây là thứ quyết định ĐỘ DÀY VẠCH (X-dimension).
   Ở 2 cột mà khung chỉ 13mm thì CHIỀU CAO là cái giới hạn (X chỉ 0.433mm); nới lên
   16mm thì bề rộng mới thành giới hạn => X = 0.533mm, trên mức danh định GS1 0.495.
   flex: 0 0 16mm để flex KHÔNG co khung lại khi nội dung tem hơi cao. */
.bl-label .bl-bar { width: 100%; height: 16mm; flex: 0 0 16mm; }
.bl-label .bl-bar svg { display: block; width: 100%; height: 100%; }
.bl-label .bl-foot { font-size: 7pt; color: #555; display: flex; justify-content: space-between; gap: 4px; }
`

/** CSS chỉ áp cho tài liệu in trong iframe. */
export const PRINT_DOC_CSS = `
@page { size: A4; margin: 8mm; }
html, body { margin: 0; padding: 0; background: #fff; }
body { font-family: -apple-system, "Segoe UI", Roboto, Arial, sans-serif; color: #000;
  -webkit-print-color-adjust: exact; print-color-adjust: exact; }
/* max-width:none — đây chính là chỗ trước kia bị ép nhỏ còn ~50%. */
.bl-preview { width: 194mm; max-width: none; margin: 0; }
.bl-label { border-color: #999; }
`

/** Dựng tài liệu in hoàn chỉnh (hàm thuần — test được). */
export function buildPrintDocument(sheetHtml) {
  return `<!DOCTYPE html><html><head><meta charset="utf-8">`
    + `<title>Tem mã vạch</title>`
    + `<style>${SHEET_CSS}${PRINT_DOC_CSS}</style>`
    + `</head><body>${sheetHtml || ''}</body></html>`
}

/**
 * Mở hộp thoại in của trình duyệt với nội dung tem trong iframe ẩn.
 * Trả về true nếu đã gọi được print().
 */
export function printSheetHtml(sheetHtml, doc = document) {
  const iframe = doc.createElement('iframe')
  iframe.setAttribute('aria-hidden', 'true')
  iframe.style.cssText =
    'position:fixed;right:0;bottom:0;width:0;height:0;border:0;visibility:hidden'
  doc.body.appendChild(iframe)

  const idoc = iframe.contentDocument
  const iwin = iframe.contentWindow
  if (!idoc || !iwin) { iframe.remove(); return false }

  idoc.open()
  idoc.write(buildPrintDocument(sheetHtml))
  idoc.close()

  const cleanup = () => { setTimeout(() => iframe.remove(), 0) }
  iwin.onafterprint = cleanup
  // đợi iframe dựng xong layout rồi mới in
  setTimeout(() => {
    try { iwin.focus(); iwin.print() } finally { if (!('onafterprint' in iwin)) cleanup() }
  }, 50)
  return true
}
