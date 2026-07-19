import { useEffect, useMemo, useRef, useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import {
  Card, Select, Table, Button, InputNumber, Space, Empty, Tag, Alert,
  Typography, Skeleton, App as AntdApp,
} from 'antd'
import { PrinterOutlined, BarcodeOutlined } from '@ant-design/icons'
import dayjs from 'dayjs'
import PageHeader from '../../components/PageHeader'
import { warehousesApi } from '../../api/warehouses.api'
import { inventoryApi } from '../../api/inventory.api'
import { useProducts } from '../../hooks/useProducts'

/**
 * IN TEM MÃ VẠCH (LÔ) — trang ĐỘC LẬP, không đụng trang nào khác.
 *
 * Luồng: chọn kho + sản phẩm → hiện danh sách lô → tick chọn → xem trước →
 * window.print() → hộp thoại in của trình duyệt tự lo máy in (USB/WiFi/PDF).
 * KHÔNG có dòng nào giao tiếp trực tiếp với máy in.
 *
 * QUYẾT ĐỊNH mã hoá (đã chốt): barcode chứa `batch.id` (khoá chính, DUY NHẤT
 * toàn hệ thống) — vì batch_number KHÔNG duy nhất (ràng buộc chỉ theo
 * product+warehouse+bin, và bộ sinh mã reset khi restart). Luồng quét khi nhặt
 * hàng sẵn có đã đối chiếu cả id lẫn batch_number nên tem id dùng được ngay.
 * Chữ NGƯỜI ĐỌC dưới mã vẫn là batch_number (+ tên SP, HSD, kho) cho dễ hiểu.
 *
 * jsbarcode nạp ĐỘNG (lazy) để không thêm KB vào các trang khác.
 */

// CSS in tem: dùng kỹ thuật visibility để ẩn toàn bộ giao diện, chỉ chừa khối
// #barcode-print-sheet — bền vững bất kể cấu trúc layout của antd.
const PRINT_CSS = `
@media print {
  body * { visibility: hidden !important; }
  #barcode-print-sheet, #barcode-print-sheet * { visibility: visible !important; }
  #barcode-print-sheet {
    position: absolute; left: 0; top: 0; width: 100%;
    background: #fff; padding: 0; margin: 0;
  }
  .bl-label { border-color: #999 !important; }
  @page { size: A4; margin: 8mm; }
}
.bl-preview { background: #fff; width: 194mm; max-width: 100%; margin: 0 auto; }
.bl-sheet {
  display: grid; grid-template-columns: repeat(3, 1fr); gap: 4mm;
}
.bl-label {
  border: 1px solid #d9d9d9; border-radius: 4px; padding: 2.5mm 3mm;
  height: 32mm; display: flex; flex-direction: column; justify-content: space-between;
  overflow: hidden; break-inside: avoid; page-break-inside: avoid;
}
.bl-label .bl-code { font-weight: 700; font-size: 11pt; line-height: 1.15;
  white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.bl-label .bl-name { font-size: 8pt; color: #333; line-height: 1.1;
  display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.bl-label .bl-bar { flex: 1; display: flex; align-items: center; min-height: 12mm; }
.bl-label .bl-bar svg { width: 100%; height: auto; max-height: 14mm; }
.bl-label .bl-foot { font-size: 7pt; color: #555; display: flex; justify-content: space-between; gap: 4px; }
`

export default function BarcodeLabelsPage() {
  const { message } = AntdApp.useApp()
  const [warehouseId, setWarehouseId] = useState()
  const [productId, setProductId] = useState()
  const [copies, setCopies] = useState(1)
  const [selectedKeys, setSelectedKeys] = useState([])
  const [barcodeLib, setBarcodeLib] = useState(null)

  const { options: productOptions, map: productMap, isLoading: loadingProducts } = useProducts()

  const whQuery = useQuery({ queryKey: ['warehouses', 'list'], queryFn: () => warehousesApi.list() })
  const warehouses = whQuery.data || []
  const whName = (id) => warehouses.find(w => w.id === id)?.name || id

  const bothChosen = !!(warehouseId && productId)
  const batchQuery = useQuery({
    queryKey: ['barcode-batches', productId, warehouseId],
    queryFn: () => inventoryApi.getBatches(productId, warehouseId),
    enabled: bothChosen,
  })
  const batches = useMemo(() => batchQuery.data || [], [batchQuery.data])

  // Đổi bộ lọc thì bỏ chọn cũ (id lô không còn trong danh sách mới) — làm ngay
  // tại handler thay vì trong effect.
  const onWarehouse = (v) => { setWarehouseId(v); setSelectedKeys([]) }
  const onProduct = (v) => { setProductId(v); setSelectedKeys([]) }

  // Nạp jsbarcode một lần khi vào trang.
  useEffect(() => {
    let alive = true
    import('jsbarcode')
      .then(mod => { if (alive) setBarcodeLib(() => (mod.default || mod)) })
      .catch(() => { if (alive) message.error('Không tải được thư viện mã vạch') })
    return () => { alive = false }
  }, [message])

  const selectedBatches = useMemo(
    () => batches.filter(b => selectedKeys.includes(b.id)),
    [batches, selectedKeys],
  )

  // Mỗi lô nhân theo số bản in. key gồm cả chỉ số bản để ref không trùng.
  const printItems = useMemo(() => {
    const n = Math.max(1, Number(copies) || 1)
    return selectedBatches.flatMap(b =>
      Array.from({ length: n }, (_, i) => ({ key: `${b.id}#${i}`, b })))
  }, [selectedBatches, copies])

  const svgRefs = useRef({})

  // Vẽ mã vạch vào từng <svg> sau khi có thư viện + danh sách in.
  useEffect(() => {
    if (!barcodeLib) return
    printItems.forEach(({ key, b }) => {
      const el = svgRefs.current[key]
      if (!el) return
      try {
        barcodeLib(el, b.id, {
          format: 'CODE128', displayValue: false, height: 48, width: 1.5, margin: 4,
        })
        // JsBarcode đặt width/height cố định (px). Chuyển sang viewBox để CSS co giãn vừa tem.
        const w = el.getAttribute('width'), h = el.getAttribute('height')
        if (w && h) {
          el.setAttribute('viewBox', `0 0 ${w} ${h}`)
          el.removeAttribute('width'); el.removeAttribute('height')
        }
      } catch {
        /* giá trị không hợp lệ cho Code128 — bỏ qua ô đó */
      }
    })
  }, [barcodeLib, printItems])

  const onPrint = () => {
    if (!printItems.length) { message.warning('Chưa chọn lô nào để in'); return }
    if (!barcodeLib) { message.info('Đang tải thư viện mã vạch, thử lại sau giây lát'); return }
    window.print()
  }

  const columns = [
    { title: 'Mã lô', dataIndex: 'batchNumber' },
    { title: 'Ô kệ', dataIndex: 'binLocation', render: (v) => v || '—' },
    { title: 'Tồn', dataIndex: 'quantity', align: 'right' },
    {
      title: 'HSD', dataIndex: 'expiryDate',
      render: (v) => v ? dayjs(v).format('DD/MM/YYYY') : '—',
    },
    {
      title: 'Trạng thái', dataIndex: 'status',
      render: (v) => <Tag color={v === 'ACTIVE' ? 'green' : v === 'HOLD' ? 'gold' : 'default'}>{v}</Tag>,
    },
  ]

  return (
    <>
      <style>{PRINT_CSS}</style>

      <PageHeader
        title={<span><BarcodeOutlined /> In tem mã vạch (lô)</span>}
        subtitle="Chọn kho và sản phẩm, tick các lô cần in, rồi bấm In — hộp thoại in của trình duyệt sẽ xử lý máy in."
        extra={
          <Button type="primary" icon={<PrinterOutlined />} onClick={onPrint}
            disabled={!printItems.length}>
            In {printItems.length ? `(${printItems.length} tem)` : ''}
          </Button>
        }
      />

      <Card size="small" style={{ marginBottom: 16 }}>
        <Space wrap size={16} align="end">
          <div>
            <div style={{ fontSize: 12, marginBottom: 4 }}>Kho</div>
            <Select
              style={{ width: 240 }} placeholder="Chọn kho" showSearch optionFilterProp="label"
              loading={whQuery.isLoading} value={warehouseId} onChange={onWarehouse}
              options={warehouses.map(w => ({ value: w.id, label: w.name }))} />
          </div>
          <div>
            <div style={{ fontSize: 12, marginBottom: 4 }}>Sản phẩm</div>
            <Select
              style={{ width: 320 }} placeholder="Chọn sản phẩm" showSearch optionFilterProp="label"
              loading={loadingProducts} value={productId} onChange={onProduct}
              options={productOptions} />
          </div>
          <div>
            <div style={{ fontSize: 12, marginBottom: 4 }}>Số bản / lô</div>
            <InputNumber min={1} max={100} value={copies} onChange={setCopies} style={{ width: 110 }} />
          </div>
        </Space>
      </Card>

      <Card
        size="small"
        title={`Danh sách lô${selectedKeys.length ? ` — đã chọn ${selectedKeys.length}` : ''}`}
        style={{ marginBottom: 16 }}>
        {!bothChosen
          ? <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="Chọn kho và sản phẩm để xem danh sách lô" />
          : batchQuery.isLoading
            ? <Skeleton active paragraph={{ rows: 4 }} title={false} />
            : batchQuery.isError
              ? <Alert type="error" showIcon message="Không tải được danh sách lô" />
              : (
                <Table
                  rowKey="id" size="small" columns={columns} dataSource={batches}
                  pagination={false}
                  rowSelection={{ selectedRowKeys: selectedKeys, onChange: setSelectedKeys }}
                  locale={{ emptyText: 'Kho/sản phẩm này chưa có lô nào' }} />
              )}
      </Card>

      <Card size="small" title="Xem trước tem in">
        {!printItems.length
          ? <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="Tick chọn lô ở bảng trên để xem trước" />
          : (
            <>
              <Typography.Text type="secondary" style={{ fontSize: 12 }}>
                {printItems.length} tem · khổ A4, 3 tem mỗi hàng. Mã vạch chứa mã định danh lô;
                dòng chữ là mã lô để đối chiếu bằng mắt.
              </Typography.Text>
              <div id="barcode-print-sheet" style={{ marginTop: 12 }}>
                <div className="bl-preview">
                  <div className="bl-sheet">
                    {printItems.map(({ key, b }) => (
                      <div className="bl-label" key={key}>
                        <div>
                          <div className="bl-code">{b.batchNumber}</div>
                          <div className="bl-name">{productMap[b.productId]?.name || b.productId}</div>
                        </div>
                        <div className="bl-bar">
                          <svg ref={(el) => { if (el) svgRefs.current[key] = el }} />
                        </div>
                        <div className="bl-foot">
                          <span>HSD: {b.expiryDate ? dayjs(b.expiryDate).format('DD/MM/YY') : '—'}</span>
                          <span>{whName(b.warehouseId)}{b.binLocation ? ` · ${b.binLocation}` : ''}</span>
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
              </div>
            </>
          )}
      </Card>
    </>
  )
}
