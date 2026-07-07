import { useEffect, useRef, useState } from 'react'
import { Alert, Button, Select, Space } from 'antd'
import { CameraOutlined, PoweroffOutlined } from '@ant-design/icons'
import { BrowserMultiFormatReader } from '@zxing/browser'

// Quét mã vạch qua camera. onScan(text) mỗi lần đọc được (đã chống lặp).
// paused=true: bỏ qua kết quả (vd đang gọi API) nhưng KHÔNG tắt camera.
export default function BarcodeScanner({ onScan, paused }) {
  const videoRef = useRef(null)
  const controlsRef = useRef(null)
  const pausedRef = useRef(paused)
  const lastRef = useRef({ code: null, t: 0 })

  const [on, setOn] = useState(false)
  const [devices, setDevices] = useState([])
  const [deviceId, setDeviceId] = useState()
  const [error, setError] = useState(null)

  const secure = typeof window !== 'undefined' && window.isSecureContext

  useEffect(() => { pausedRef.current = paused }, [paused])

  useEffect(() => {
    if (!on) { stop(); return }
    let cancelled = false
    ;(async () => {
      try {
        setError(null)
        if (!navigator.mediaDevices?.getUserMedia) throw new Error('Trình duyệt không hỗ trợ camera (getUserMedia).')
        // Xin quyền trước (mở khoá nhãn thiết bị), rồi tắt stream tạm.
        const tmp = await navigator.mediaDevices.getUserMedia({ video: { facingMode: { ideal: 'environment' } } })
        tmp.getTracks().forEach(t => t.stop())
        const cams = (await navigator.mediaDevices.enumerateDevices()).filter(d => d.kind === 'videoinput')
        if (cancelled) return
        setDevices(cams)
        const rear = cams.find(d => /back|rear|sau|environment/i.test(d.label || ''))
        const chosen = deviceId || rear?.deviceId || cams[cams.length - 1]?.deviceId
        if (chosen !== deviceId) setDeviceId(chosen)
        const reader = new BrowserMultiFormatReader()
        controlsRef.current = await reader.decodeFromVideoDevice(chosen, videoRef.current, (result) => {
          if (!result || pausedRef.current) return
          const text = result.getText()
          const now = Date.now()
          if (text === lastRef.current.code && now - lastRef.current.t < 2500) return // chống quét lặp
          lastRef.current = { code: text, t: now }
          onScan(text)
        })
      } catch (e) {
        if (!cancelled) { setError(mapErr(e)); setOn(false) }
      }
    })()
    return () => { cancelled = true; stop() }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [on, deviceId])

  function stop() { try { controlsRef.current?.stop() } catch { /* noop */ } }

  if (!secure) {
    return (
      <Alert type="warning" showIcon message="Cần HTTPS để dùng camera"
        description='Chạy "npm run dev:mobile" trên máy tính, rồi trên điện thoại mở https://<IP-máy>:5173 (chấp nhận cảnh báo chứng chỉ).' />
    )
  }

  return (
    <div>
      {error && <Alert type="error" showIcon style={{ marginBottom: 8 }} message="Lỗi camera" description={error} />}
      {!on ? (
        <Button type="primary" block size="large" icon={<CameraOutlined />} onClick={() => setOn(true)}>Bật camera</Button>
      ) : (
        <>
          <div style={{ position: 'relative', background: '#000', borderRadius: 8, overflow: 'hidden' }}>
            <video ref={videoRef} style={{ width: '100%', maxHeight: 340, objectFit: 'cover', display: 'block' }}
              muted autoPlay playsInline />
            <div style={{ position: 'absolute', inset: 0, pointerEvents: 'none', boxShadow: 'inset 0 0 0 3px rgba(255,255,255,.5)', borderRadius: 8 }} />
          </div>
          <Space style={{ marginTop: 8, width: '100%', justifyContent: 'space-between' }}>
            {devices.length > 1
              ? <Select size="small" style={{ minWidth: 170 }} value={deviceId} onChange={setDeviceId}
                  options={devices.map((d, i) => ({ value: d.deviceId, label: d.label || `Camera ${i + 1}` }))} />
              : <span />}
            <Button size="small" icon={<PoweroffOutlined />} onClick={() => setOn(false)}>Tắt</Button>
          </Space>
        </>
      )}
    </div>
  )
}

function mapErr(e) {
  const n = e?.name
  if (n === 'NotAllowedError' || n === 'SecurityError') return 'Bị từ chối quyền camera. Vào cài đặt trình duyệt (biểu tượng khoá cạnh URL) cho phép Camera rồi thử lại.'
  if (n === 'NotFoundError' || n === 'OverconstrainedError') return 'Không tìm thấy camera phù hợp trên thiết bị.'
  if (n === 'NotReadableError') return 'Camera đang bị ứng dụng khác chiếm dụng. Đóng app camera/khác rồi thử lại.'
  return e?.message || 'Không mở được camera.'
}
