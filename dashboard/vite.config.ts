import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import fs from 'fs'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    https: fs.existsSync('/etc/ssl/private/ssl-cert-snakeoil.key') && fs.existsSync('/etc/ssl/certs/ssl-cert-snakeoil.pem') ? {
      key: fs.readFileSync('/etc/ssl/private/ssl-cert-snakeoil.key'),
      cert: fs.readFileSync('/etc/ssl/certs/ssl-cert-snakeoil.pem')
    } : undefined
  }
})
