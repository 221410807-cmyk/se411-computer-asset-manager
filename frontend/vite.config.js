import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// Vite settings for the app and for the Vitest test runner.
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173, // The Spring Boot backend allows requests from this port (CORS).
  },
  test: {
    environment: 'jsdom', // A fake browser so React components can render in tests.
    globals: true,
    setupFiles: './src/setupTests.js',
    // Tests always use the standalone (local) data mode unless a test injects another service.
    env: { VITE_DATA_MODE: 'local' },
  },
});
