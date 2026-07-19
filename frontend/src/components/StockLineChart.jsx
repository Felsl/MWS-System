import { theme, Empty } from 'antd'
import dayjs from 'dayjs'

/**
 * Biểu đồ ĐƯỜNG Xuất-Nhập-Tồn theo ngày — TỰ VẼ BẰNG SVG, KHÔNG THÊM THƯ VIỆN.
 *
 * Cùng triết lý với StockBarChart: Dashboard là trang đích sau đăng nhập, mọi
 * người dùng đều tải, nên tránh nhét recharts (~400KB) vào đường tải quan trọng.
 * Ba đường (Nhập, Xuất, Tồn) dùng chung một trục Y (max của cả ba) để trung thực;
 * màu lấy từ design token nên tự đúng theo nền sáng/tối.
 *
 * props: data = [{ date, inQty, outQty, closingQty }]
 */
export default function StockLineChart({ data = [], height = 220 }) {
  const { token } = theme.useToken()

  if (!data.length) {
    return <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="Chưa có dữ liệu biến động" />
  }

  const W = 720, H = height
  const padL = 8, padR = 8, padT = 12, padB = 22
  const innerW = W - padL - padR
  const innerH = H - padT - padB
  const n = data.length

  const max = Math.max(1, ...data.map(d => Math.max(d.inQty || 0, d.outQty || 0, d.closingQty || 0)))
  const x = (i) => padL + (n === 1 ? innerW / 2 : (i * innerW) / (n - 1))
  const y = (v) => padT + innerH - ((v || 0) / max) * innerH
  const fmt = (v) => new Intl.NumberFormat('vi-VN').format(v || 0)

  const line = (key) => data.map((d, i) => `${x(i)},${y(d[key])}`).join(' ')

  const series = [
    { key: 'closingQty', label: 'Tồn', color: token.colorPrimary },
    { key: 'inQty', label: 'Nhập', color: token.colorSuccess },
    { key: 'outQty', label: 'Xuất', color: token.colorError },
  ]

  // Chỉ gắn nhãn ngày ở đầu / giữa / cuối để tránh chữ chồng nhau.
  const tickIdx = n <= 1 ? [0] : [0, Math.floor((n - 1) / 2), n - 1]

  return (
    <div>
      <div style={{ display: 'flex', gap: 16, marginBottom: 8, fontSize: 12 }}>
        {series.map(s => (
          <span key={s.key} style={{ display: 'inline-flex', alignItems: 'center', gap: 6 }}>
            <i style={{ width: 14, height: 3, borderRadius: 2, background: s.color, display: 'inline-block' }} />
            {s.label}
          </span>
        ))}
      </div>

      <svg viewBox={`0 0 ${W} ${H}`} width="100%" role="img" preserveAspectRatio="none"
        aria-label={`Xuất-Nhập-Tồn ${data.length} ngày, tồn cuối gần nhất ${fmt(data[n - 1].closingQty)}`}>
        {/* trục đáy */}
        <line x1={padL} y1={padT + innerH} x2={W - padR} y2={padT + innerH}
          stroke={token.colorBorderSecondary} strokeWidth="1" />
        {series.map(s => (
          <polyline key={s.key} fill="none" stroke={s.color} strokeWidth="2"
            strokeLinejoin="round" strokeLinecap="round" points={line(s.key)} />
        ))}
        {/* điểm + tooltip trình duyệt */}
        {series.map(s => data.map((d, i) => (
          <circle key={`${s.key}-${i}`} cx={x(i)} cy={y(d[s.key])} r="2.5" fill={s.color}>
            <title>{`${dayjs(d.date).format('DD/MM')} · ${s.label}: ${fmt(d[s.key])}`}</title>
          </circle>
        )))}
        {/* nhãn ngày */}
        {tickIdx.map(i => (
          <text key={`t-${i}`} x={x(i)} y={H - 6} fontSize="11" fill={token.colorTextSecondary}
            textAnchor={i === 0 ? 'start' : i === n - 1 ? 'end' : 'middle'}>
            {dayjs(data[i].date).format('DD/MM')}
          </text>
        ))}
      </svg>
    </div>
  )
}
