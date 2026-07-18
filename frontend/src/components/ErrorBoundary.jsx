import { Component } from 'react'
import { Button, Result, Typography } from 'antd'
import { ReloadOutlined, HomeOutlined } from '@ant-design/icons'

/**
 * Chặn lỗi render. Không có nó thì một lỗi JS bất kỳ trong 1 trang sẽ làm
 * React unmount toàn bộ cây => màn hình TRẮNG, không thông báo, không thao tác
 * được gì (rủi ro thật khi demo trực tiếp).
 *
 * Dùng 2 tầng:
 *  - trong AppLayout, bọc <Outlet/> (key=pathname) => hỏng 1 trang, menu vẫn sống.
 *  - trong main.jsx, bọc toàn app => chốt chặn cuối cùng.
 *
 * props:
 *  - onGoHome: (tuỳ chọn) hàm điều hướng về '/'. Không truyền thì dùng href.
 */
export default class ErrorBoundary extends Component {
  constructor(props) {
    super(props)
    this.state = { error: null, info: null }
  }

  static getDerivedStateFromError(error) {
    return { error }
  }

  componentDidCatch(error, info) {
    // Giữ lại để xem trong console khi dev / khi người dùng báo lỗi.
    console.error('[MWS] Lỗi render:', error, info?.componentStack)
    this.setState({ info })
  }

  reset = () => this.setState({ error: null, info: null })

  render() {
    const { error, info } = this.state
    if (!error) return this.props.children

    const isDev = import.meta.env?.DEV

    return (
      <Result
        status="500"
        title="Trang gặp lỗi"
        subTitle="Đã có lỗi ngoài dự kiến khi hiển thị trang này. Dữ liệu của bạn không bị ảnh hưởng."
        extra={[
          <Button key="retry" type="primary" icon={<ReloadOutlined />} onClick={this.reset}>
            Thử lại
          </Button>,
          <Button key="reload" icon={<ReloadOutlined />} onClick={() => window.location.reload()}>
            Tải lại trang
          </Button>,
          <Button
            key="home" icon={<HomeOutlined />}
            onClick={() => {
              this.reset()
              if (this.props.onGoHome) this.props.onGoHome()
              else window.location.href = '/'
            }}>
            Về trang chủ
          </Button>,
        ]}>
        {isDev && (
          <details style={{ textAlign: 'left', maxWidth: 720, margin: '0 auto' }}>
            <summary style={{ cursor: 'pointer' }}>Chi tiết lỗi (chỉ hiện khi dev)</summary>
            <Typography.Paragraph>
              <pre style={{ whiteSpace: 'pre-wrap', fontSize: 12, marginTop: 8 }}>
                {String(error?.stack || error)}
                {info?.componentStack}
              </pre>
            </Typography.Paragraph>
          </details>
        )}
      </Result>
    )
  }
}
