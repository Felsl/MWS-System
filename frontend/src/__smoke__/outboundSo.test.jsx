import { describe, it, expect } from 'vitest'
import { sortPickingLists } from '../features/outbound/pickingSort'
import { soStatusMeta, SO_STATUS_OPTS } from '../features/outbound/soStatus'

describe('sortPickingLists — đang lấy lên trước', () => {
  const rows = [
    { pickNumber: 'PK-03', status: 'COMPLETED', startedAt: '2026-07-20T10:00:00' },
    { pickNumber: 'PK-01', status: 'PENDING', startedAt: null },
    { pickNumber: 'PK-04', status: 'PICKING', startedAt: '2026-07-21T08:00:00' },
    { pickNumber: 'PK-02', status: 'PICKING', startedAt: '2026-07-22T08:00:00' },
  ]

  it('nhóm theo thứ tự Đang lấy -> Chờ lấy -> Hoàn thành', () => {
    expect(sortPickingLists(rows).map(r => r.status))
      .toEqual(['PICKING', 'PICKING', 'PENDING', 'COMPLETED'])
  })

  it('trong cùng nhóm thì mới nhất lên trước', () => {
    const picking = sortPickingLists(rows).filter(r => r.status === 'PICKING')
    expect(picking.map(r => r.pickNumber)).toEqual(['PK-02', 'PK-04'])
  })

  it('không sửa mảng gốc (cache của React Query)', () => {
    const original = [...rows]
    sortPickingLists(rows)
    expect(rows).toEqual(original)
  })

  it('chịu được mảng rỗng / phần tử thiếu trường', () => {
    expect(sortPickingLists()).toEqual([])
    expect(() => sortPickingLists([{}, null])).not.toThrow()
  })
})

describe('soStatusMeta — nhãn đơn bán sau khi lấy xong', () => {
  it('PICKING mà lệnh lấy chưa xong -> vẫn là Đang lấy hàng', () => {
    expect(soStatusMeta('PICKING', false).label).toBe('Đang lấy hàng')
  })

  it('PICKING mà lệnh lấy đã xong -> đổi nhãn', () => {
    expect(soStatusMeta('PICKING', true).label).toBe('Hoàn thành lấy hàng')
  })

  it('các trạng thái khác không bị ảnh hưởng bởi picked', () => {
    expect(soStatusMeta('SHIPPED', true).label).toBe('Đã xuất')
    expect(soStatusMeta('ALLOCATED', true).label).toBe('Đã phân bổ')
  })

  it('nhãn "Hoàn thành lấy hàng" KHÔNG lọt vào bộ lọc trạng thái', () => {
    expect(SO_STATUS_OPTS.map(o => o.value)).not.toContain('PICKED')
    expect(SO_STATUS_OPTS.map(o => o.label)).not.toContain('Hoàn thành lấy hàng')
  })
})
