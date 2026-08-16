import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// Konfiguracija Vite razvojnog servera.
// Frontend radi na portu 5173, a backend na 8080 - ta dva porta su
// dozvoljena u CORS konfiguraciji na backendu (SecurityConfig).
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    open: true
  }
})
