import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import basicSsl from '@vitejs/plugin-basic-ssl'

// Dev proxy: FE -> BE (8080). Không cần BE bật CORS khi dev.
// Chạy thường:      npm run dev        (http, localhost)
// Chạy cho điện thoại: npm run dev:mobile  (https + expose LAN, để camera hoạt động)
export default defineConfig(({ mode }) => {
  const mobile = mode === 'mobile'
  return {
    plugins: [react(), mobile && basicSsl()].filter(Boolean),
    // sockjs-client tham chiếu biến 'global' của Node -> map sang globalThis.
    define: { global: 'globalThis' },
    build: {
      // KHÔNG dùng manualChunks — cố ý.
      //
      // Đã thử gom vendor thủ công (vendor-react / vendor-antd / vendor) và kết
      // quả TỆ HƠN mặc định ở cả hai mặt:
      //   1. Sinh vòng lặp chunk: react-dom kéo `scheduler` (rơi vào 'vendor')
      //      => vendor-react -> vendor; còn react-query trong 'vendor' lại kéo
      //      react => vendor -> vendor-react. Rollup cảnh báo "Circular chunk".
      //      Vá bằng cách liệt kê thêm scheduler/cookie/... là trò đuổi bắt.
      //   2. Nặng hơn: ép TOÀN BỘ antd (1,1 MB) vào chunk khởi động. Rollup mặc
      //      định tự tách antd theo component — vd Table thành chunk riêng
      //      165 KB, chỉ tải khi vào trang có bảng.
      //
      // Số đo thật (cùng source):
      //   gốc, 1 bundle        2.660 KB thô / 812 KB gzip
      //   manualChunks thủ công 1.531 KB / 487 KB  + 2 cảnh báo circular
      //   mặc định (bản này)    1.028 KB / 330 KB  + không cảnh báo
      //
      // Việc tách xlsx/@zxing/sockjs KHÔNG cần manualChunks: chúng chỉ được với
      // tới qua `await import()` (utils/excel.js, BarcodeScanner,
      // ws/notificationClient.js) nên Rollup vốn đã đẩy ra chunk riêng.
      //
      // Chunk khởi động ~1 MB gần như toàn bộ là antd core + locale + dayjs,
      // dùng ở mọi trang nên không tách thêm được. Nâng ngưỡng cảnh báo cho
      // đúng thực tế thay vì để build kêu mỗi lần.
      chunkSizeWarningLimit: 1100,
    },
    server: {
      port: 5173,
      host: mobile,            // expose ra LAN khi chạy mobile
      proxy: {
        '/api': { target: 'http://localhost:8080', changeOrigin: true },
        '/ws': { target: 'http://localhost:8080', changeOrigin: true, ws: true },
      },
    },
  }
})
