import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      // En desarrollo, /api se redirige a la API de Spring Boot
      '/api': {
        target: 'http://localhost:31026',
        changeOrigin: true,
      },
    },
  },
})
