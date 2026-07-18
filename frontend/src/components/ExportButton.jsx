import { useState } from 'react'
import { Button, App as AntdApp } from 'antd'
import { DownloadOutlined } from '@ant-design/icons'
import { exportRows, preloadXLSX } from '../utils/excel'

/**
 * Nút "Xuất Excel" cho trang danh sách. Gọi `fetchRows()` để lấy TẤT CẢ dòng
 * đang lọc (không chỉ trang hiện tại), rồi xuất ra .xlsx.
 *  - fetchRows: () => Promise<row[]>  (dòng dạng object; mỗi key thành 1 cột)
 *  - filename:  tên file .xlsx
 *  - map:       (tuỳ chọn) (row) => object đã chọn/đổi tên cột trước khi xuất
 *
 * Thư viện xlsx nay được nạp động (xem utils/excel.js). Rê chuột vào nút là bắt
 * đầu tải chunk ngầm, nên tới lúc bấm thật thì thường đã sẵn sàng.
 */
export default function ExportButton({ fetchRows, filename = 'export.xlsx', map }) {
  const { message } = AntdApp.useApp()
  const [loading, setLoading] = useState(false)

  const run = async () => {
    setLoading(true)
    try {
      const raw = await fetchRows()
      const rows = Array.isArray(raw) ? raw : (raw?.content || raw?.data || [])
      if (!rows.length) { message.info('Không có dữ liệu để xuất'); return }
      await exportRows(map ? rows.map(map) : rows, filename)
    } catch {
      message.error('Xuất Excel thất bại')
    } finally {
      setLoading(false)
    }
  }

  return (
    <Button icon={<DownloadOutlined />} loading={loading} onClick={run}
      onMouseEnter={preloadXLSX} onFocus={preloadXLSX}>Xuất Excel</Button>
  )
}
