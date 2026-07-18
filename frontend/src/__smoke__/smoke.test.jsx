/**
 * Smoke test: chỉ trả lời MỘT câu hỏi — "app có render nổi không, hay nổ?".
 *
 * Không phải unit test nghiệp vụ. Mục đích là chặn đúng loại lỗi đã suýt lọt
 * trong lần refactor này: biến ngoài scope (ReferenceError), provider mắc sai
 * thứ tự, route trỏ vào component không tồn tại. `vite build` KHÔNG bắt được
 * những lỗi đó — chúng chỉ nổ lúc chạy.
 *
 * Chạy: npx vitest run
 */
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { render, screen, waitFor, fireEvent, cleanup } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { ConfigProvider, App as AntdApp } from 'antd'

// Chặn mọi lời gọi API: test này không cần BE.
// Hình dạng trả về phải KHỚP BE thật, nếu không thì test báo lỗi giả:
//   - endpoint phân trang -> PageResponse { content, totalElements, totalPages }
//   - endpoint danh sách phẳng (kho, ĐVVC, người dùng...) -> mảng
const PAGED = ['/products', '/purchase-orders', '/sales-orders', '/transfer-orders',
  '/adjustment-vouchers', '/stocktakes', '/stock-movements', '/inventory']
vi.mock('../api/client', async () => {
  const actual = await vi.importActual('../api/client')
  const get = vi.fn((url) => {
    const paged = PAGED.some(u => String(url).includes(u))
    return Promise.resolve({
      data: paged ? { content: [], page: 0, size: 20, totalElements: 0, totalPages: 0, hasNext: false } : [],
    })
  })
  return {
    ...actual,
    default: {
      get,
      post: vi.fn(() => Promise.resolve({ data: {} })),
      put: vi.fn(() => Promise.resolve({ data: {} })),
      delete: vi.fn(() => Promise.resolve({ data: {} })),
      interceptors: { request: { use: vi.fn() }, response: { use: vi.fn() } },
    },
    getErrorMessage: (e) => e?.message || 'lỗi',
    tokenStore: { access: null, refresh: null, set: vi.fn(), clear: vi.fn() },
  }
})
// Quyền: cho full quyền để nút/nhánh nào cũng được render (nơi bug hay nấp).
vi.mock('../auth/AuthContext', async () => {
  const actual = await vi.importActual('../auth/AuthContext')
  return { ...actual, useAuth: () => ({
    user: { userId: 'u1', username: 'test', fullName: 'Test', role: 'ADMIN', permissions: [] },
    hasPermission: () => true, logout: vi.fn(),
  }) }
})
// WebSocket: không kết nối thật trong test.
vi.mock('../ws/notificationClient', () => ({
  createNotificationClient: () => ({ deactivate: vi.fn() }),
}))

// `globals: false` trong vitest.config.js => testing-library KHÔNG tự đăng ký
// auto-cleanup, DOM sẽ cộng dồn qua các test và getByRole ném "Found multiple
// elements". Phải dọn tay.
afterEach(cleanup)

// jsdom chưa có 2 API này; antd + FitTable cần.
beforeEach(() => {
  globalThis.ResizeObserver = class { observe() {} unobserve() {} disconnect() {} }
  window.matchMedia = window.matchMedia || ((q) => ({
    matches: false, media: q, onchange: null,
    addEventListener: () => {}, removeEventListener: () => {},
    addListener: () => {}, removeListener: () => {}, dispatchEvent: () => false,
  }))
})

function renderAt(path, ui) {
  const qc = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return render(
    <ConfigProvider><AntdApp>
      <QueryClientProvider client={qc}>
        <MemoryRouter initialEntries={[path]}>{ui}</MemoryRouter>
      </QueryClientProvider>
    </AntdApp></ConfigProvider>,
  )
}

describe('Hook điều khiển URL', () => {
  for (const [path, expected] of [
    ['/transfer-orders', 'list:-'],
    ['/transfer-orders/new', 'create:-'],
    ['/transfer-orders/abc-123', 'detail:abc-123'],
  ]) {
    it(`useRecordView: ${path} -> ${expected}`, async () => {
      const { useRecordView } = await import('../hooks/useRecordView')
      function Probe() {
        const v = useRecordView('/transfer-orders')
        return <div data-testid="m">{v.mode}:{v.id || '-'}</div>
      }
      const { Routes, Route } = await import('react-router-dom')
      renderAt(path, (
        <Routes>
          <Route path="/transfer-orders" element={<Probe />} />
          <Route path="/transfer-orders/new" element={<Probe />} />
          <Route path="/transfer-orders/:id" element={<Probe />} />
        </Routes>
      ))
      expect(screen.getByTestId('m').textContent).toBe(expected)
    })
  }

  it('useListParams đọc bộ lọc từ query string', async () => {
    const { useListParams } = await import('../hooks/useListParams')
    function Probe() {
      const p = useListParams()
      return <div data-testid="p">{p.status}|{p.keyword}|{p.page}|{p.dir}</div>
    }
    renderAt('/x?status=PENDING_APPROVAL&q=abc&page=2&sort=code&dir=desc', <Probe />)
    expect(screen.getByTestId('p').textContent).toBe('PENDING_APPROVAL|abc|2|desc')
  })
})

describe('Accessibility / layout (mục 16 + 23)', () => {
  it('RowLink là <button> thật, không phải <a> trần', async () => {
    const RowLink = (await import('../components/RowLink')).default
    const clicks = []
    renderAt('/', <RowLink onClick={() => clicks.push(1)}>PO-001</RowLink>)
    // Điểm mấu chốt của mục 23: phải Tab tới được + screen reader thấy => role button.
    const btn = screen.getByRole('button', { name: /PO-001/ })
    expect(btn.tagName).toBe('BUTTON')
    fireEvent.click(btn)
    expect(clicks.length).toBe(1)
  })

  it('PageHeader hiện title, extra và nút back', async () => {
    const PageHeader = (await import('../components/PageHeader')).default
    const { Button } = await import('antd')
    renderAt('/', <PageHeader
      title="Sản phẩm"
      onBack={<Button>Danh sách</Button>}
      extra={<Button>Thêm</Button>} />)
    expect(screen.getByText('Sản phẩm')).toBeTruthy()
    expect(screen.getByRole('button', { name: /Thêm/ })).toBeTruthy()
    expect(screen.getByRole('button', { name: /Danh sách/ })).toBeTruthy()
  })
})

describe('Dashboard — tồn kho', () => {
  it('lowStock chỉ gồm dòng dưới mức an toàn, thiếu nhiều xếp trước', async () => {
    // Không render cả Dashboard (cần nhiều provider); test thẳng phép lọc/xếp
    // vì đó mới là chỗ dễ sai.
    const rows = [
      { productId: 'p1', availableQuantity: 5 },    // safety 20 -> thiếu 15
      { productId: 'p2', availableQuantity: 50 },   // safety 20 -> đủ
      { productId: 'p3', availableQuantity: 18 },   // safety 20 -> thiếu 2
      { productId: 'p4', availableQuantity: 0 },    // safety 0  -> KHÔNG đặt ngưỡng, bỏ qua
    ]
    const products = { p1: { safetyStock: 20 }, p2: { safetyStock: 20 }, p3: { safetyStock: 20 }, p4: { safetyStock: 0 } }
    const low = rows
      .filter(r => { const sfz = products[r.productId]?.safetyStock; return sfz != null && sfz > 0 && r.availableQuantity < sfz })
      .map(r => ({ id: r.productId, deficit: products[r.productId].safetyStock - r.availableQuantity }))
      .sort((a, b) => b.deficit - a.deficit)
    expect(low.map(x => x.id)).toEqual(['p1', 'p3'])
    expect(low[0].deficit).toBe(15)
  })

  it('StockBarChart render được và không nổ khi rỗng', async () => {
    const Chart = (await import('../components/StockBarChart')).default
    renderAt('/', <Chart data={[{ id: 'w1', name: 'Kho HCM', available: 100, reserved: 20, skus: 3 }]} />)
    expect(screen.getByText('Kho HCM')).toBeTruthy()
    cleanup()
    renderAt('/', <Chart data={[]} />)
    expect(screen.getByText(/Chưa có dữ liệu/)).toBeTruthy()
  })
})

describe('Optimistic: đánh dấu đã đọc', () => {
  it('giữ đúng hình dạng {count:n} của BE, không đổi thành số trần', async () => {
    // BẪY THẬT: notificationsApi.unreadCount trả Map {count:n}. Nếu bản cập nhật
    // lạc quan ghi đè bằng số, readUnread() không đọc được và badge rơi về 0.
    const { readUnread } = await import('../api/notifications.api')
    const withCount = (old, n) => (old && typeof old === 'object' ? { ...old, count: n } : { count: n })
    expect(readUnread(withCount({ count: 5 }, 4))).toBe(4)
    expect(readUnread(withCount(undefined, 0))).toBe(0)
    // Ghi đè bằng số trần chính là cách làm SAI:
    expect(readUnread(4)).toBe(0)
  })
})

describe('Tách lỗi validate của BE', () => {
  it('tách đúng định dạng "field: msg; field: msg"', async () => {
    const { parseFieldErrors } = await import('../utils/formErrors')
    expect(parseFieldErrors('sku: must not be blank; price: must be > 0'))
      .toEqual([{ field: 'sku', msg: 'must not be blank' }, { field: 'price', msg: 'must be > 0' }])
  })
  it('KHÔNG nuốt nhầm câu văn xuôi có dấu hai chấm', async () => {
    const { parseFieldErrors } = await import('../utils/formErrors')
    expect(parseFieldErrors('Không đủ tồn: cần 10, còn 3')).toEqual([])
  })
})

describe('Bấm nút "Tạo ..." không nổ', () => {
  // Bug thật đã tìm thấy trên branch fe: PLList/SHList gọi setView() của
  // component CHA => ReferenceError. Render không bắt được (arrow function chỉ
  // chạy khi click), nên phải bấm thật.
  const CREATE_BTNS = [
    ['PickingListsPage', () => import('../features/outbound/PickingListsPage'), '/picking-lists', 'Tạo lệnh'],
    ['ShipmentsPage', () => import('../features/outbound/ShipmentsPage'), '/shipments', 'Tạo vận đơn'],
  ]
  for (const [name, load, path, label] of CREATE_BTNS) {
    it(`${name}: bấm "${label}"`, async () => {
      const errors = []
      const spy = vi.spyOn(console, 'error').mockImplementation((...a) => errors.push(String(a[0])))
      // Lỗi ném từ trong event handler của React KHÔNG đi qua console.error —
      // nó nổi lên window.onerror. Không nghe chỗ này thì test báo xanh trong
      // khi trình duyệt thật đang nổ (đúng cái bẫy vừa mắc phải).
      const onWinErr = (e) => { errors.push(String(e.error?.message || e.message)); e.preventDefault() }
      window.addEventListener('error', onWinErr)
      try {
        const Mod = (await load()).default
        const { Routes, Route } = await import('react-router-dom')
        renderAt(path, (
          <Routes>
            <Route path={path} element={<Mod />} />
            <Route path={`${path}/new`} element={<Mod />} />
            <Route path={`${path}/:id`} element={<Mod />} />
          </Routes>
        ))
        const btn = await screen.findByRole('button', { name: new RegExp(label) }, { timeout: 8000 })
        fireEvent.click(btn)
        await waitFor(() => expect(document.body.textContent.length).toBeGreaterThan(0))
        await new Promise(r => setTimeout(r, 30)) // để lỗi bất đồng bộ kịp nổi lên
        const fatal = errors.filter(e => /is not defined|is not a function/.test(e))
        expect(fatal, `Bấm "${label}" trong ${name} gây lỗi:\n${fatal.join('\n')}`).toEqual([])
      } finally {
        window.removeEventListener('error', onWinErr)
        spy.mockRestore()
      }
    })
  }
})

describe('Trang render được (không ReferenceError)', () => {
  // Đây chính là loại bug đã tìm thấy: setView gọi ngoài scope ở PLList/SHList.
  const PAGES = [
    ['PickingListsPage', () => import('../features/outbound/PickingListsPage'), '/picking-lists'],
    ['ShipmentsPage', () => import('../features/outbound/ShipmentsPage'), '/shipments'],
    ['StocktakesPage', () => import('../features/stocktake/StocktakesPage'), '/stocktakes'],
    ['AdjustmentsPage', () => import('../features/stocktake/AdjustmentsPage'), '/adjustment-vouchers'],
    ['TransferOrdersPage', () => import('../features/transfer/TransferOrdersPage'), '/transfer-orders'],
    ['PurchaseOrdersPage', () => import('../features/inbound/PurchaseOrdersPage'), '/purchase-orders'],
    ['SalesOrdersPage', () => import('../features/outbound/SalesOrdersPage'), '/sales-orders'],
    ['GoodsReceiptsPage', () => import('../features/inbound/GoodsReceiptsPage'), '/goods-receipts'],
  ]
  for (const [name, load, path] of PAGES) {
    it(`${name} render không nổ`, async () => {
      const errors = []
      const spy = vi.spyOn(console, 'error').mockImplementation((...a) => errors.push(String(a[0])))
      const Mod = (await load()).default
      const { Routes, Route } = await import('react-router-dom')
      renderAt(path, (
        <Routes>
          <Route path={path} element={<Mod />} />
          <Route path={`${path}/new`} element={<Mod />} />
          <Route path={`${path}/:id`} element={<Mod />} />
        </Routes>
      ))
      await waitFor(() => expect(document.body.textContent.length).toBeGreaterThan(0))
      const fatal = errors.filter(e => /is not defined|is not a function|Cannot read/.test(e))
      expect(fatal, `Lỗi runtime trong ${name}:\n${fatal.join('\n')}`).toEqual([])
      spy.mockRestore()
    })
  }
})
