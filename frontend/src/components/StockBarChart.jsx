import { theme, Empty, Tooltip } from 'antd'

/**
 * Biểu đồ cột ngang xếp chồng: tồn khả dụng + đang giữ, theo từng kho.
 *
 * TỰ VẼ BẰNG SVG, KHÔNG THÊM THƯ VIỆN — cố ý:
 * Dashboard là trang đích sau khi đăng nhập, MỌI người dùng đều tải. Thêm
 * recharts/@ant-design/plots (~400KB) là nhét thẳng vào đường tải quan trọng,
 * xoá sạch công code-split ở round 2. Một biểu đồ cột thì ~80 dòng SVG là đủ,
 * tốn ~2KB, và tự đúng màu theo nền sáng/tối qua design token.
 *
 * Cột ngang (không phải dọc) vì nhãn là tên kho — chữ dài, nằm ngang mới đọc được.
 *
 * props: data = [{ id, name, available, reserved, skus }]
 */
export default function StockBarChart({ data = [], height = 26, gap = 12 }) {
  const { token } = theme.useToken()

  if (!data.length) {
    return <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="Chưa có dữ liệu tồn kho" />
  }

  const max = Math.max(1, ...data.map(d => (d.available || 0) + (d.reserved || 0)))
  const fmt = (n) => new Intl.NumberFormat('vi-VN').format(n || 0)

  return (
    <div>
      {/* Chú giải */}
      <div style={{ display: 'flex', gap: 16, marginBottom: 10, fontSize: 12 }}>
        <Legend color={token.colorPrimary} label="Khả dụng" />
        <Legend color={token.colorWarning} label="Đang giữ (đã phân bổ)" />
      </div>

      <div role="img" aria-label={`Tồn kho theo kho: ${data.map(d => `${d.name} ${fmt(d.available + d.reserved)}`).join('; ')}`}>
        {data.map((d) => {
          const total = (d.available || 0) + (d.reserved || 0)
          const pctA = ((d.available || 0) / max) * 100
          const pctR = ((d.reserved || 0) / max) * 100
          return (
            <div key={d.id} style={{ marginBottom: gap }}>
              <div style={{
                display: 'flex', justifyContent: 'space-between',
                fontSize: 12, marginBottom: 4, gap: 8,
              }}>
                <span style={{ overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{d.name}</span>
                <span style={{ color: token.colorTextSecondary, whiteSpace: 'nowrap' }}>
                  {fmt(total)} · {d.skus} SKU
                </span>
              </div>
              <div style={{
                display: 'flex', height, width: '100%',
                background: token.colorFillTertiary, borderRadius: token.borderRadiusSM, overflow: 'hidden',
              }}>
                <Tooltip title={`Khả dụng: ${fmt(d.available)}`}>
                  <div style={{ width: `${pctA}%`, background: token.colorPrimary, transition: 'width .3s' }} />
                </Tooltip>
                <Tooltip title={`Đang giữ (đơn đã phân bổ, chưa xuất): ${fmt(d.reserved)}`}>
                  <div style={{ width: `${pctR}%`, background: token.colorWarning, transition: 'width .3s' }} />
                </Tooltip>
              </div>
            </div>
          )
        })}
      </div>
    </div>
  )
}

function Legend({ color, label }) {
  return (
    <span style={{ display: 'inline-flex', alignItems: 'center', gap: 6 }}>
      <i style={{ width: 10, height: 10, borderRadius: 2, background: color, display: 'inline-block' }} />
      {label}
    </span>
  )
}
