/**
 * [lat7] Tự phân bổ số lượng nhập vào các ô kệ theo SỨC CHỨA (kg + thể tích).
 *
 * Chiến lược (đã chốt với người dùng):
 *  1) Ưu tiên đổ vào ô ĐÃ CÓ cùng sản phẩm trước (gom hàng), ô nào trống nhiều xếp trước.
 *  2) Còn dư thì đổ first-fit sang các ô khác (đầy ô này mới sang ô kế).
 *  3) Một sản phẩm CÓ THỂ tách ra nhiều ô -> sinh nhiều dòng nhập.
 *  4) Nếu tổng sức chứa không đủ, phần dư dồn vào ô còn trống nhất (vượt hạn mức)
 *     để CẢNH BÁO MỀM sẵn có kích hoạt; người dùng vẫn sửa tay được.
 *
 * Hàm THUẦN (không đụng React/form) để test độc lập. Chỉ dùng chiều đo có hạn mức
 * (max > 0); sản phẩm thiếu weight/volume coi như 0 ở chiều đó.
 *
 * @param lines  mảng dòng nhập hiện tại: { productId, quantity, ...giữ nguyên các field khác }
 * @param bins   từ listBins: { id, coordinateLabel, maxWeight, maxVolume, occupiedWeight, occupiedVolume }
 * @param productMap  id -> { weight, volume }
 * @param existingBinsByProduct  { productId: Set(binLocationId đã có sản phẩm đó) }
 * @returns mảng dòng MỚI (đã tách theo ô), mỗi dòng có binLocationId + quantity.
 */
export function allocateBins(lines, bins, productMap = {}, existingBinsByProduct = {}) {
  const binList = Array.isArray(bins) ? bins : []
  // Sức chứa còn lại theo ô (mutate dần khi phân bổ nhiều dòng vào cùng ô).
  const remW = {}
  const remV = {}
  const limW = {}
  const limV = {}
  for (const b of binList) {
    const mw = Number(b.maxWeight || 0)
    const mv = Number(b.maxVolume || 0)
    limW[b.id] = mw > 0
    limV[b.id] = mv > 0
    remW[b.id] = mw > 0 ? mw - Number(b.occupiedWeight || 0) : Infinity
    remV[b.id] = mv > 0 ? mv - Number(b.occupiedVolume || 0) : Infinity
  }

  // Số đơn vị còn nhét được vào 1 ô theo cả 2 chiều (Infinity nếu không giới hạn).
  const unitsFit = (binId, unitW, unitV) => {
    let fit = Infinity
    if (limW[binId] && unitW > 0) fit = Math.min(fit, Math.floor(remW[binId] / unitW))
    if (limV[binId] && unitV > 0) fit = Math.min(fit, Math.floor(remV[binId] / unitV))
    return fit
  }
  const take = (binId, units, unitW, unitV) => {
    if (Number.isFinite(remW[binId])) remW[binId] -= units * unitW
    if (Number.isFinite(remV[binId])) remV[binId] -= units * unitV
  }

  const out = []
  for (const line of (lines || [])) {
    const rest = { ...(line || {}) }
    delete rest.binLocationId
    delete rest.quantity
    let qty = Math.max(0, Number(line?.quantity) || 0)
    if (!rest.productId || qty <= 0 || binList.length === 0) {
      out.push({ ...line }) // không đủ dữ liệu để phân bổ -> giữ nguyên
      continue
    }
    const p = productMap[rest.productId] || {}
    const unitW = Number(p.weight || 0)
    const unitV = Number(p.volume || 0)

    const already = existingBinsByProduct[rest.productId] || new Set()
    const priority = binList.filter(b => already.has(b.id))
    const others = binList.filter(b => !already.has(b.id))
    // Ô ưu tiên: trống nhiều xếp trước (gom nhưng không làm tràn sớm).
    const freeScore = (b) => Math.min(
      Number.isFinite(remW[b.id]) ? remW[b.id] : Number.MAX_SAFE_INTEGER,
      Number.isFinite(remV[b.id]) ? remV[b.id] : Number.MAX_SAFE_INTEGER,
    )
    priority.sort((a, b) => freeScore(b) - freeScore(a))
    const ordered = [...priority, ...others]

    for (const b of ordered) {
      if (qty <= 0) break
      const fit = unitsFit(b.id, unitW, unitV)
      const a = Math.min(qty, Number.isFinite(fit) ? fit : qty)
      if (a <= 0) continue
      out.push({ ...rest, binLocationId: b.id, quantity: a })
      take(b.id, a, unitW, unitV)
      qty -= a
    }

    // Còn dư: dồn vào ô còn trống nhất (kể cả vượt hạn mức) để cảnh báo mềm bật.
    if (qty > 0) {
      const target = [...ordered].sort((a, b) => freeScore(b) - freeScore(a))[0]
      out.push({ ...rest, binLocationId: target ? target.id : undefined, quantity: qty })
      if (target) take(target.id, qty, unitW, unitV)
    }
  }
  return out
}
