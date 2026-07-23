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
import { fitBarcodeSvg } from './barcodeSvg'
import { SHEET_CSS, printSheetHtml } from './printSheet'

/**
 * IN TEM MÃ VẠCH (LÔ) — trang ĐỘC LẬP, không đụng trang nào khác.
 *
 * Luồng: chọn kho + sản phẩm → hiện danh sách lô → tick chọn → xem trước →
 * bấm In → dựng iframe sạch rồi gọi print() (xem printSheet.js) → hộp thoại in
 * của trình duyệt tự lo máy in (USB/WiFi/PDF).
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
  const sheetRef = useRef(null)

  // Vẽ mã vạch vào từng <svg> sau khi có thư viện + danh sách in.
  useEffect(() => {
    if (!barcodeLib) return
    printItems.forEach(({ key, b }) => {
      const el = svgRefs.current[key]
      if (!el) return
      try {
        barcodeLib(el, b.id, {
          format: 'CODE128', displayValue: false, height: 60, width: 2,
          // QUIET ZONE bắt buộc của Code128: >= 10 module trắng mỗi đầu, nếu
          // không máy quét không bắt được start/stop pattern. width:2 => 20 đơn vị.
          // Giữ margin:0 cho trên/dưới; KHÔNG đặt marginTop:0 vì JsBarcode dùng
          // `marginTop || margin` nên số 0 bị hiểu thành mặc định 10.
          margin: 0, marginLeft: 20, marginRight: 20,
        })
        fitBarcodeSvg(el)
      } catch {
        /* giá trị không hợp lệ cho Code128 — bỏ qua ô đó */
      }
    })
  }, [barcodeLib, printItems])

  const onPrint = () => {
    if (!printItems.length) { message.warning('Chưa chọn lô nào để in'); return }
    if (!barcodeLib) { message.info('Đang tải thư viện mã vạch, thử lại sau giây lát'); return }
    // In qua iframe sạch: CSS của app (antd Card position:relative + sidebar vẫn
    // chiếm chỗ) từng làm tem co còn ~50% khi in. Xem printSheet.js.
    if (!printSheetHtml(sheetRef.current?.innerHTML)) {
      message.error('Không mở được hộp thoại in')
    }
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
      <style>{SHEET_CSS}</style>

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
                {printItems.length} tem · khổ A4, 2 tem mỗi hàng. Mã vạch chứa mã định danh lô;
                dòng chữ là mã lô để đối chiếu bằng mắt.
              </Typography.Text>
              <div id="barcode-print-sheet" ref={sheetRef} style={{ marginTop: 12 }}>
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
