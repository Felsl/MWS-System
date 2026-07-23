/**
 * Chuẩn hoá <svg> do JsBarcode sinh ra để co giãn vừa khung tem mà KHÔNG méo.
 *
 * JsBarcode ĐÃ tự đặt viewBox đúng dạng số trần ("0 0 286 60") và đồng thời đặt
 * width="286px" / height="60px" (bin/renderers/svg.js:151-155).
 * Chỉ cần BỎ width/height để svg co theo khung CSS.
 *
 * TUYỆT ĐỐI KHÔNG dựng lại viewBox từ getAttribute('width'): giá trị đó kèm đuôi
 * "px", tạo ra viewBox "0 0 286px 60px" KHÔNG HỢP LỆ -> trình duyệt bỏ qua ->
 * svg mất tỷ lệ nội tại -> mã vạch bị CẮT CỤT (mất checksum + stop pattern, máy
 * quét không đọc được) và méo tỷ lệ khi in. Đây chính là lỗi đã từng xảy ra.
 */
export function fitBarcodeSvg(el) {
  if (!el) return
  el.setAttribute('preserveAspectRatio', 'xMidYMid meet')
  el.removeAttribute('width')
  el.removeAttribute('height')
}
