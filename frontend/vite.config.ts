import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  root: '.',
  plugins: [react()],
  server: {
    port: 5173,
    open: true,
    proxy: {
      '/api': {
        target: 'http://localhost:8080', // gateway-service
        changeOrigin: true,
        ws: true, // support websockets
        secure: false,
      },
    },
  },
});
