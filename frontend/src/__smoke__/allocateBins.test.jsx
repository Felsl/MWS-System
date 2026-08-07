import { describe, it, expect } from 'vitest'
import { allocateBins } from '../features/inbound/allocateBins'

const bins = [
  { id: 'B1', coordinateLabel: 'A-1', maxWeight: 100, maxVolume: 0, occupiedWeight: 0, occupiedVolume: 0 },
  { id: 'B2', coordinateLabel: 'A-2', maxWeight: 100, maxVolume: 0, occupiedWeight: 0, occupiedVolume: 0 },
]
const productMap = { P1: { weight: 10, volume: 0 } } // 10kg/đơn vị -> 10 đơn vị/ô (100kg)

describe('allocateBins', () => {
  it('tách 1 sản phẩm ra nhiều ô khi 1 ô không đủ (first-fit)', () => {
    const out = allocateBins([{ productId: 'P1', quantity: 15, supplierId: 'S1' }], bins, productMap, {})
    expect(out).toHaveLength(2)
    expect(out[0]).toMatchObject({ binLocationId: 'B1', quantity: 10, supplierId: 'S1' })
    expect(out[1]).toMatchObject({ binLocationId: 'B2', quantity: 5 })
  })

  it('ưu tiên ô đã có cùng sản phẩm trước', () => {
    const existing = { P1: new Set(['B2']) }
    const out = allocateBins([{ productId: 'P1', quantity: 5 }], bins, productMap, existing)
    expect(out).toHaveLength(1)
    expect(out[0].binLocationId).toBe('B2') // B2 đã có P1 -> đổ vào B2 trước
  })

  it('không đủ sức chứa -> dồn phần dư (vượt hạn mức) để cảnh báo bật', () => {
    const out = allocateBins([{ productId: 'P1', quantity: 25 }], bins, productMap, {})
    const total = out.reduce((s, l) => s + l.quantity, 0)
    expect(total).toBe(25) // không mất số lượng
    expect(out.length).toBeGreaterThanOrEqual(2)
  })

  it('không có hạn mức -> dồn hết vào 1 ô', () => {
    const noLimit = [{ id: 'B1', maxWeight: 0, maxVolume: 0, occupiedWeight: 0, occupiedVolume: 0 }]
    const out = allocateBins([{ productId: 'P1', quantity: 999 }], noLimit, productMap, {})
    expect(out).toHaveLength(1)
    expect(out[0]).toMatchObject({ binLocationId: 'B1', quantity: 999 })
  })

  it('giữ nguyên dòng thiếu dữ liệu (chưa chọn SP)', () => {
    const out = allocateBins([{ quantity: 5 }], bins, productMap, {})
    expect(out).toHaveLength(1)
    expect(out[0].binLocationId).toBeUndefined()
  })
})
