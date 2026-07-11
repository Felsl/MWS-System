import { useMemo, useState } from 'react'
import {
  Card, Select, Button, Upload, Space, Typography, Alert, Table, Tag, Progress, App as AntdApp,
} from 'antd'
import { DownloadOutlined, UploadOutlined, PlayCircleOutlined } from '@ant-design/icons'
import { downloadTemplate, parseWorkbook } from '../../utils/excel'
import { productsApi } from '../../api/products.api'
import { categoriesApi } from '../../api/categories.api'
import { suppliersApi, customersApi } from '../../api/partners.api'

const { Title, Text, Paragraph } = Typography

// Cấu hình từng loại dữ liệu. `fields` giống các trường khi tạo từng cái.
// kind: str | int | num | bool  -> quyết định cách ép kiểu ô Excel.
const TYPES = {
  products: {
    label: 'Sản phẩm',
    api: productsApi,
    note: 'categoryId phải là ID nhóm hợp lệ; unit là mã ĐVT (VD: PCS, BOX, KG...).',
    fields: [
      { key: 'sku', required: true, kind: 'str' },
      { key: 'name', required: true, kind: 'str' },
      { key: 'unit', required: true, kind: 'str' },
      { key: 'categoryId', kind: 'str' },
      { key: 'barcode', kind: 'str' },
      { key: 'safetyStock', kind: 'int' },
      { key: 'price', kind: 'num' },
      { key: 'costPrice', kind: 'num' },
      { key: 'weight', kind: 'num' },
      { key: 'volume', kind: 'num' },
      { key: 'hazardousFlag', kind: 'bool' },
      { key: 'description', kind: 'str' },
    ],
  },
  categories: {
    label: 'Nhóm sản phẩm',
    api: categoriesApi,
    fields: [
      { key: 'code', required: true, kind: 'str' },
      { key: 'name', required: true, kind: 'str' },
      { key: 'description', kind: 'str' },
    ],
  },
  suppliers: {
    label: 'Nhà cung cấp',
    api: suppliersApi,
    fields: [
      { key: 'code', required: true, kind: 'str' },
      { key: 'name', required: true, kind: 'str' },
      { key: 'contactName', kind: 'str' },
      { key: 'phone', kind: 'str' },
      { key: 'email', kind: 'str' },
      { key: 'address', kind: 'str' },
    ],
  },
  customers: {
    label: 'Khách hàng',
    api: customersApi,
    fields: [
      { key: 'code', required: true, kind: 'str' },
      { key: 'name', required: true, kind: 'str' },
      { key: 'taxCode', kind: 'str' },
      { key: 'phone', kind: 'str' },
      { key: 'email', kind: 'str' },
      { key: 'address', kind: 'str' },
    ],
  },
}

function coerce(kind, raw) {
  if (raw === '' || raw === null || raw === undefined) return undefined
  if (kind === 'int') { const n = parseInt(raw, 10); return Number.isNaN(n) ? undefined : n }
  if (kind === 'num') { const n = Number(raw); return Number.isNaN(n) ? undefined : n }
  if (kind === 'bool') {
    const s = String(raw).trim().toLowerCase()
    return s === 'true' || s === '1' || s === 'x' || s === 'yes' || s === 'có'
  }
  return String(raw).trim()
}

// row (object theo header) -> { payload, error }
function buildPayload(cfg, row) {
  const payload = {}
  for (const f of cfg.fields) {
    const v = coerce(f.kind, row[f.key])
    if (v !== undefined) payload[f.key] = v
    if (f.required && (v === undefined || v === '')) {
      return { error: `Thiếu trường bắt buộc: ${f.key}` }
    }
  }
  return { payload }
}

export default function DataIOPage() {
  const { message } = AntdApp.useApp()
  const [type, setType] = useState('products')
  const [rows, setRows] = useState([])          // dữ liệu đọc từ file
  const [fileName, setFileName] = useState('')
  const [running, setRunning] = useState(false)
  const [progress, setProgress] = useState(0)
  const [results, setResults] = useState(null)  // { total, ok, fail, errors:[{row,message}] }

  const cfg = TYPES[type]
  const headers = useMemo(() => cfg.fields.map(f => f.key), [cfg])

  const onPickType = (t) => { setType(t); setRows([]); setFileName(''); setResults(null); setProgress(0) }

  const beforeUpload = async (file) => {
    try {
      const data = await parseWorkbook(file)
      setRows(data)
      setFileName(file.name)
      setResults(null)
      setProgress(0)
      message.success(`Đã đọc ${data.length} dòng từ "${file.name}"`)
    } catch {
      message.error('Không đọc được file. Cần .xlsx hoặc .csv đúng định dạng mẫu.')
    }
    return false // chặn upload tự động của antd
  }

  const runImport = async () => {
    if (!rows.length) { message.info('Chưa có dữ liệu — hãy tải mẫu, điền và chọn file.'); return }
    setRunning(true); setProgress(0)
    const errors = []
    let ok = 0
    for (let i = 0; i < rows.length; i++) {
      const excelRow = i + 2 // dòng 1 là tiêu đề
      const { payload, error } = buildPayload(cfg, rows[i])
      if (error) { errors.push({ row: excelRow, message: error }); }
      else {
        try { await cfg.api.create(payload); ok++ }
        catch (e) {
          const msg = e?.response?.data?.message || e?.message || 'Lỗi không xác định'
          errors.push({ row: excelRow, message: msg })
        }
      }
      setProgress(Math.round(((i + 1) / rows.length) * 100))
    }
    setResults({ total: rows.length, ok, fail: errors.length, errors })
    setRunning(false)
    if (errors.length === 0) message.success(`Nhập thành công ${ok}/${rows.length} dòng`)
    else message.warning(`Xong: ${ok} thành công, ${errors.length} lỗi`)
  }

  return (
    <div style={{ maxWidth: 900 }}>
      <Title level={4}>Nhập dữ liệu từ Excel</Title>
      <Paragraph type="secondary">
        Chọn loại dữ liệu → tải file mẫu → điền dữ liệu → chọn file để nhập. Cột trong file mẫu trùng tên với
        các trường khi tạo thủ công.
      </Paragraph>

      <Card>
        <Space direction="vertical" size="large" style={{ width: '100%' }}>
          <Space wrap align="end">
            <div>
              <div style={{ marginBottom: 4 }}><Text strong>Loại dữ liệu</Text></div>
              <Select style={{ width: 220 }} value={type} onChange={onPickType}
                options={Object.entries(TYPES).map(([k, v]) => ({ value: k, label: v.label }))} />
            </div>
            <Button icon={<DownloadOutlined />}
              onClick={() => downloadTemplate(headers, `mau-${type}.xlsx`)}>
              Tải file mẫu
            </Button>
            <Upload beforeUpload={beforeUpload} maxCount={1} showUploadList={false}
              accept=".xlsx,.xls,.csv">
              <Button icon={<UploadOutlined />}>Chọn file dữ liệu</Button>
            </Upload>
            <Button type="primary" icon={<PlayCircleOutlined />}
              onClick={runImport} loading={running} disabled={!rows.length}>
              Bắt đầu nhập {rows.length ? `(${rows.length} dòng)` : ''}
            </Button>
          </Space>

          {cfg.note && <Alert type="info" showIcon message={cfg.note} />}
          {fileName && <Text type="secondary">File: {fileName} — {rows.length} dòng</Text>}
          {running && <Progress percent={progress} />}

          {results && (
            <>
              <Space>
                <Tag color="blue">Tổng: {results.total}</Tag>
                <Tag color="green">Thành công: {results.ok}</Tag>
                <Tag color={results.fail ? 'red' : 'default'}>Lỗi: {results.fail}</Tag>
              </Space>
              {results.errors.length > 0 && (
                <Table size="small" rowKey={(r) => r.row} pagination={{ pageSize: 10 }}
                  columns={[
                    { title: 'Dòng', dataIndex: 'row', width: 90 },
                    { title: 'Lỗi', dataIndex: 'message' },
                  ]}
                  dataSource={results.errors} />
              )}
            </>
          )}
        </Space>
      </Card>
    </div>
  )
}
