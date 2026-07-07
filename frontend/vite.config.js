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
