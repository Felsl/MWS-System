import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// Dev proxy: FE (5173) -> BE (8080). Không cần BE bật CORS khi dev.
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': { target: 'http://localhost:8080', changeOrigin: true },
      '/ws':  { target: 'http://localhost:8080', changeOrigin: true, ws: true },
    },
  },
})
