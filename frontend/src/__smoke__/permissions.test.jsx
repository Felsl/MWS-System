/**
 * Test riêng cho các component phụ thuộc quyền (Can, EmptyState) + nhãn quyền.
 *
 * TÁCH KHỎI smoke.test.jsx CÓ CHỦ ĐÍCH: file này mock '../auth/AuthContext' theo
 * TỪNG ca (vi.doMock + import động), còn smoke.test.jsx mock nó TOPLEVEL = full
 * quyền. Trộn hai kiểu trong một file thì doMock cục bộ rò sang test khác, và
 * resetModules để dọn lại phá luôn mock toplevel. Mỗi file một chiến lược mock =
 * hết xung đột.
 */
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { render, screen, fireEvent, cleanup } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { ConfigProvider, App as AntdApp } from 'antd'

beforeEach(() => {
  globalThis.ResizeObserver = class { observe() {} unobserve() {} disconnect() {} }
  window.matchMedia = window.matchMedia || ((q) => ({
    matches: false, media: q, onchange: null,
    addEventListener: () => {}, removeEventListener: () => {},
    addListener: () => {}, removeListener: () => {}, dispatchEvent: () => false,
  }))
})
afterEach(() => { cleanup(); vi.resetModules() })

function renderUI(ui) {
  return render(
    <ConfigProvider><AntdApp>
      <MemoryRouter>{ui}</MemoryRouter>
    </AntdApp></ConfigProvider>,
  )
}

// Mock useAuth theo quyền được cấp, rồi import Can SAU khi mock đã đặt.
async function withPerms(granted) {
  vi.resetModules()
  vi.doMock('../auth/AuthContext', () => ({
    useAuth: () => ({ hasPermission: (p) => granted.includes(p) }),
  }))
  return {
    Can: (await import('../components/Can')).default,
    EmptyState: (await import('../components/EmptyState')).default,
  }
}

describe('Can — khoá nút thay vì ẩn (mục 21)', () => {
  it('CÓ quyền: nút bình thường', async () => {
    const { Can } = await withPerms(['USER_CREATE'])
    const { Button } = await import('antd')
    renderUI(<Can permission="USER_CREATE"><Button>Tạo</Button></Can>)
    expect(screen.getByRole('button', { name: /Tạo/ }).disabled).toBe(false)
  })
  it('THIẾU quyền: nút bị khoá chứ KHÔNG biến mất', async () => {
    const { Can } = await withPerms([])
    const { Button } = await import('antd')
    renderUI(<Can permission="USER_CREATE"><Button>Tạo</Button></Can>)
    expect(screen.getByRole('button', { name: /Tạo/ }).disabled).toBe(true)
  })
  it('THIẾU quyền + con KHÔNG phải nút: vẫn ẩn', async () => {
    const { Can } = await withPerms([])
    const { Tag } = await import('antd')
    renderUI(<Can permission="USER_CREATE"><Tag>Khối</Tag></Can>)
    expect(screen.queryByText('Khối')).toBeNull()
  })
  it('mode="hide" ép ẩn kể cả khi con là nút', async () => {
    const { Can } = await withPerms([])
    const { Button } = await import('antd')
    renderUI(<Can permission="USER_CREATE" mode="hide"><Button>Tạo</Button></Can>)
    expect(screen.queryByRole('button', { name: /Tạo/ })).toBeNull()
  })
})

describe('EmptyState — CTA theo quyền (mục 22)', () => {
  it('CÓ quyền: mô tả + nút CTA, bấm được', async () => {
    const { EmptyState } = await withPerms(['MASTER_PRODUCT_MANAGE'])
    const clicks = []
    renderUI(<EmptyState
      title="Chưa có sản phẩm nào"
      action={{ label: 'Thêm sản phẩm', onClick: () => clicks.push(1), permission: 'MASTER_PRODUCT_MANAGE' }}
      secondary={{ label: 'Nhập từ Excel', onClick: () => {} }} />)
    expect(screen.getByText('Chưa có sản phẩm nào')).toBeTruthy()
    expect(screen.getByRole('button', { name: /Nhập từ Excel/ })).toBeTruthy()
    fireEvent.click(screen.getByRole('button', { name: /Thêm sản phẩm/ }))
    expect(clicks.length).toBe(1)
  })
  it('không permission: CTA luôn hiện', async () => {
    const { EmptyState } = await withPerms([])
    renderUI(<EmptyState title="Trống" action={{ label: 'Tạo mới', onClick: () => {} }} />)
    // action không kèm permission => Can cho qua bất kể quyền.
    expect(screen.getByRole('button', { name: /Tạo mới/ })).toBeTruthy()
  })
})

describe('Nhãn quyền', () => {
  it('MỌI mã trong P đều có nhãn tiếng Việt', async () => {
    const { P, PERM_LABELS } = await import('../constants/permissions')
    const thieu = Object.values(P).filter(code => !PERM_LABELS[code])
    expect(thieu, `Thiếu nhãn cho: ${thieu.join(', ')}`).toEqual([])
  })
})
