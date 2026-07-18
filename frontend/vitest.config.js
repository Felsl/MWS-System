import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  define: { global: 'globalThis' },
  test: {
    environment: 'jsdom',
    globals: false,
    include: ['src/**/*.test.jsx'],
    // Lần đầu import một trang lớn (TransferOrdersPage + antd) có thể mất >1s;
    // timeout mặc định 5s của vitest và 1s của findBy* từng làm suite đỏ giả một
    // lần trên máy chậm. Nới ra cho khỏi nhấp nháy — test này không đo tốc độ.
    testTimeout: 20000,
  },
})
