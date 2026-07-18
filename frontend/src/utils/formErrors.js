/**
 * Đưa lỗi validate của BE về đúng ô nhập trong form.
 *
 * BE (GlobalExceptionHandler) gộp mọi field error thành MỘT chuỗi:
 *     "sku: must not be blank; price: must be greater than 0"
 *   (xem formatFieldError + Collectors.joining("; "))
 *
 * FE trước đây chỉ `message.error(chuỗi đó)` -> toast đỏ 3 giây rồi biến mất,
 * form không đánh dấu ô nào sai, người dùng phải tự nhớ. Với form 12 ô như
 * "Thêm sản phẩm" thì đây là cách nhanh nhất để người ta bỏ cuộc.
 *
 * Hàm dưới tách ngược chuỗi đó ra và gắn vào đúng Form.Item.
 */

/** "sku: must not be blank; price: ..." -> [{ field:'sku', msg:'...' }, ...] */
export function parseFieldErrors(message) {
  if (!message || typeof message !== 'string') return []
  return message
    .split(';')
    .map((part) => {
      // Chỉ tách ở dấu ':' ĐẦU TIÊN — bản thân thông báo có thể chứa ':'.
      const i = part.indexOf(':')
      if (i <= 0) return null
      const field = part.slice(0, i).trim()
      const msg = part.slice(i + 1).trim()
      // Tên field hợp lệ mới nhận; tránh nuốt nhầm câu văn xuôi có dấu ':'
      // (vd "Không đủ tồn: cần 10, còn 3" -> KHÔNG phải lỗi field).
      if (!msg || !/^[a-zA-Z_][a-zA-Z0-9_.]*$/.test(field)) return null
      return { field, msg }
    })
    .filter(Boolean)
}

/**
 * Gắn lỗi BE vào form.
 *
 * @returns true  nếu đã gắn được ít nhất 1 ô (phía gọi KHÔNG cần toast nữa)
 *          false nếu không phải lỗi field (lỗi nghiệp vụ/mạng) -> phía gọi tự toast
 *
 * Chỉ nhận lỗi 400. Lỗi 403/409/500 là lỗi nghiệp vụ, không thuộc về ô nào.
 * Field nào không có trong form (BE đặt tên khác) thì bỏ qua, để rơi về toast —
 * không bao giờ nuốt lỗi im lặng.
 */
export function applyServerErrors(form, err) {
  if (err?.response?.status !== 400) return false
  const parsed = parseFieldErrors(err?.response?.data?.message)
  if (!parsed.length) return false

  const known = new Set(
    (form.getFieldsValue(true) ? Object.keys(form.getFieldsValue(true)) : []),
  )
  const hits = parsed.filter(p => known.has(p.field))
  if (!hits.length) return false

  form.setFields(hits.map(({ field, msg }) => ({ name: field, errors: [msg] })))
  // Cuộn tới ô sai đầu tiên: form dài thì lỗi có thể nằm ngoài màn hình.
  try { form.scrollToField(hits[0].field, { behavior: 'smooth', block: 'center' }) } catch { /* noop */ }
  return true
}

/**
 * Dùng trong onError của mutation:
 *     onError: (e) => handleFormError(form, e, message)
 * Gắn được vào ô -> im lặng. Không gắn được -> toast như cũ.
 */
export function handleFormError(form, err, message, fallback) {
  if (applyServerErrors(form, err)) return
  const text = err?.response?.data?.message || err?.message || fallback || 'Đã có lỗi xảy ra'
  message.error(text)
}
