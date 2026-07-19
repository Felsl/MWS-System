/**
 * [MỤC 6] StockLineChart — biểu đồ Xuất-Nhập-Tồn tự vẽ bằng SVG.
 * Chốt 2 điều: có dữ liệu thì vẽ ra <svg>; rỗng thì hiện Empty (không nổ).
 */
import { describe, it, expect, afterEach } from 'vitest'
import { render, screen, cleanup } from '@testing-library/react'
import { ConfigProvider } from 'antd'
import StockLineChart from '../components/StockLineChart'

afterEach(cleanup)

const sample = [
  { date: '2026-07-01', inQty: 10, outQty: 4, closingQty: 106 },
  { date: '2026-07-02', inQty: 0, outQty: 8, closingQty: 98 },
  { date: '2026-07-03', inQty: 20, outQty: 5, closingQty: 113 },
]

const wrap = (ui) => render(<ConfigProvider>{ui}</ConfigProvider>)

describe('StockLineChart', () => {
  it('vẽ SVG khi có dữ liệu, kèm chú giải 3 đường', () => {
    wrap(<StockLineChart data={sample} />)
    expect(screen.getByRole('img')).toBeTruthy()
    expect(screen.getByText('Nhập')).toBeTruthy()
    expect(screen.getByText('Xuất')).toBeTruthy()
    expect(screen.getByText('Tồn')).toBeTruthy()
  })

  it('hiện Empty khi không có dữ liệu', () => {
    wrap(<StockLineChart data={[]} />)
    expect(screen.queryByRole('img')).toBeNull()
    expect(screen.getByText('Chưa có dữ liệu biến động')).toBeTruthy()
  })
})
