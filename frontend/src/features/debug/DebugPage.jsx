import { useEffect, useState } from 'react'
import { Card, Descriptions, Tag, Typography, Alert, Button, Space, Spin } from 'antd'
import api from '../../api/client'

/**
 * Trang chẩn đoán id→tên (mở /debug trên CẢ PC lẫn điện thoại rồi so sánh).
 *
 * 7 mục "hiện id thay vì tên" đều lấy field tên (createdByName, assignedToName,
 * batchNumber, warehouseName, supplierName) THẲNG từ JSON của backend. Cùng code
 * FE cho mọi màn hình => nếu 2 máy ra khác nhau thì do JSON khác nhau (máy đang
 * gọi backend cũ / khác origin / bị cache phía server). Trang này in ra:
 *   - Backend + origin mà THIẾT BỊ NÀY thực sự đang dùng.
 *   - Giá trị tên thô từng endpoint, kèm verdict CÓ TÊN / THIẾU TÊN.
 * Chụp màn hình 2 máy đặt cạnh nhau là biết ngay lỗi nằm ở đâu.
 */

// Đổi chuỗi này rồi rebuild để chắc chắn thiết bị đang chạy bundle MỚI, không phải cache.
const BUILD_MARKER = 'debug-v1'

function field(label, name, id) {
  const ok = name != null && name !== '' && name !== id
  return {
    key: label,
    label,
    children: (
      <Space wrap>
        <span>tên: <b>{name == null || name === '' ? '(null)' : String(name)}</b></span>
        <Typography.Text type="secondary">id: {id ?? '(null)'}</Typography.Text>
        {ok ? <Tag color="green">CÓ TÊN</Tag> : <Tag color="red">THIẾU TÊN → sẽ hiện id</Tag>}
      </Space>
    ),
  }
}

export default function DebugPage() {
  const [state, setState] = useState({ loading: true })

  useEffect(() => {
    let alive = true
    ;(async () => {
      const out = { rows: [], errors: [] }
      // 1) Sales order (createdByName). List KHÔNG có /v1.
      try {
        const so = (await api.get('/api/sales-orders', { params: { size: 1 } })).data?.content?.[0]
        if (so) out.rows.push(field('SO · Người tạo', so.createdByName, so.createdBy))
        else out.errors.push('Không có đơn bán nào để kiểm tra.')
      } catch (e) { out.errors.push('sales-orders: ' + (e?.response?.status || e.message)) }

      // 2+3) Picking list (assignedToName + batchNumber trong details).
      try {
        const list = (await api.get('/api/v1/picking-lists')).data
        const pl0 = Array.isArray(list) ? list[0] : (list?.content?.[0])
        if (pl0) {
          const pl = (await api.get(`/api/v1/picking-lists/${pl0.id}`)).data
          out.rows.push(field('Picking · Người lấy', pl.assignedToName, pl.assignedTo))
          const d = (pl.details || [])[0]
          out.rows.push(field('Picking · Lô cần (dòng đầu)', d?.batchNumber, d?.batchId))
        } else out.errors.push('Không có lệnh lấy hàng nào để kiểm tra.')
      } catch (e) { out.errors.push('picking-lists: ' + (e?.response?.status || e.message)) }

      // 5+6) Stock demand (warehouseName + supplierName).
      try {
        const dem = (await api.get('/api/v1/stock-demands')).data
        const d0 = Array.isArray(dem) ? dem[0] : (dem?.content?.[0])
        if (d0) {
          out.rows.push(field('Nhu cầu · Kho', d0.warehouseName, d0.warehouseId))
          out.rows.push(field('Nhu cầu · NCC', d0.supplierName, d0.supplierId))
        } else out.errors.push('Không có dòng nhu cầu nào (mục Kho/NCC không kiểm tra được).')
      } catch (e) { out.errors.push('stock-demands: ' + (e?.response?.status || e.message)) }

      if (alive) setState({ loading: false, ...out })
    })()
    return () => { alive = false }
  }, [])

  const env = {
    'BUILD_MARKER (phải là debug-v1)': BUILD_MARKER,
    'VITE_API_BASE_URL (base bundle build)': import.meta.env.VITE_API_BASE_URL || '(rỗng → dùng proxy /api)',
    'MODE': import.meta.env.MODE,
    'PROD': String(import.meta.env.PROD),
    'window.location.origin': window.location.origin,
    'window.location.host': window.location.host,
  }

  return (
    <div style={{ maxWidth: 760, margin: '0 auto' }}>
      <Card title="Chẩn đoán id → tên (mở trên CẢ PC và điện thoại rồi so sánh)">
        <Alert type="info" showIcon style={{ marginBottom: 12 }}
          message="Cách đọc"
          description="Mở /debug trên PC và trên điện thoại. Nếu PC ra 'CÓ TÊN' còn điện thoại ra 'THIẾU TÊN' thì lỗi KHÔNG ở giao diện — mà do máy đó gọi tới backend cũ/khác. Đối chiếu 2 dòng 'Backend/Origin' bên dưới để biết mỗi máy đang gọi đi đâu." />

        <Typography.Title level={5} style={{ marginTop: 0 }}>Thiết bị này đang dùng</Typography.Title>
        <Descriptions size="small" column={1} bordered
          items={Object.entries(env).map(([k, v]) => ({ key: k, label: k, children: <code>{v}</code> }))} />

        <Typography.Title level={5}>Field tên từ backend</Typography.Title>
        {state.loading ? <Spin /> : (
          <>
            {state.rows?.length > 0 && (
              <Descriptions size="small" column={1} bordered items={state.rows} />
            )}
            {state.errors?.length > 0 && (
              <Alert type="warning" showIcon style={{ marginTop: 12 }}
                message="Một số endpoint không đọc được"
                description={<ul style={{ margin: 0, paddingLeft: 18 }}>{state.errors.map((e, i) => <li key={i}>{e}</li>)}</ul>} />
            )}
          </>
        )}

        <div style={{ marginTop: 16 }}>
          <Button onClick={() => window.location.reload()}>Tải lại</Button>
        </div>
      </Card>
    </div>
  )
}
