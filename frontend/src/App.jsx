import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { BrowserRouter } from 'react-router-dom'
import { ConfigProvider, App as AntdApp, theme } from 'antd'
import viVN from 'antd/locale/vi_VN'
import { AuthProvider } from './auth/AuthContext.jsx'
import { NotificationProvider } from './context/NotificationContext.jsx'
import { useThemeMode } from './context/ThemeContext.jsx'
import ErrorBoundary from './components/ErrorBoundary.jsx'
import AppRoutes from './routes/index.jsx'

// Tồn kho có optimistic-lock (409) => cho retry nhẹ; lỗi 4xx khác thì không retry.
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: (failureCount, err) => {
        const s = err?.response?.status
        if (s && s >= 400 && s < 500 && s !== 409) return false
        return failureCount < 2
      },
      staleTime: 30_000,
      refetchOnWindowFocus: false,
    },
  },
})

// Đứng TRONG <ThemeProvider> (xem main.jsx) để ConfigProvider đọc được chế độ
// sáng/tối. Tách khỏi main.jsx để main.jsx chỉ còn mỗi việc mount — react-refresh
// yêu cầu file chứa component phải export component đó.
export default function App() {
  const { isDark } = useThemeMode()
  return (
    <ConfigProvider
      locale={viVN}
      theme={{
        algorithm: isDark ? theme.darkAlgorithm : theme.defaultAlgorithm,
        // Nhận diện riêng cho MWS. Trước đây colorPrimary đúng bằng #1677ff mặc
        // định của antd => app "trông như demo antd". Teal hợp một WMS: mát, sạch,
        // gợi kho/logistics, và tách bạch với màu cảnh báo (đỏ/cam) vốn xuất hiện
        // nhiều trong app kho. Bo góc lớn hơn một chút + màu chức năng dịu lại
        // cho đồng bộ. Mọi màu đều qua token nên tự đúng ở cả nền sáng/tối.
        token: {
          colorPrimary: '#0D9488',
          colorInfo: '#0D9488',
          colorSuccess: '#16A34A',
          colorWarning: '#D97706',
          colorError: '#DC2626',
          colorLink: '#0F766E',
          borderRadius: 8,
          fontFamily: "-apple-system, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif",
        },
        components: {
          // Bảng là màn hình chính của app kho => hàng tiêu đề đậm màu thương
          // hiệu nhạt cho dễ phân biệt vùng đầu bảng, dòng chọn cũng theo teal.
          Table: {
            headerBg: isDark ? undefined : '#F0FDFA',
            rowSelectedBg: isDark ? undefined : '#CCFBF1',
            rowSelectedHoverBg: isDark ? undefined : '#99F6E4',
          },
          Menu: {
            itemSelectedBg: isDark ? undefined : '#CCFBF1',
            itemSelectedColor: isDark ? undefined : '#0F766E',
          },
          Layout: {
            // logo/sider để trắng, không nhuộm — teal chỉ dùng cho điểm nhấn.
            triggerBg: '#0D9488',
          },
        },
      }}>
      <AntdApp>
        {/* Chốt chặn cuối: lỗi ở tầng provider/route cũng không để màn hình trắng.
            (AppLayout còn 1 boundary nữa bọc riêng từng trang.) */}
        <ErrorBoundary>
          <QueryClientProvider client={queryClient}>
            <BrowserRouter>
              <AuthProvider>
                <NotificationProvider>
                  <AppRoutes />
                </NotificationProvider>
              </AuthProvider>
            </BrowserRouter>
          </QueryClientProvider>
        </ErrorBoundary>
      </AntdApp>
    </ConfigProvider>
  )
}
