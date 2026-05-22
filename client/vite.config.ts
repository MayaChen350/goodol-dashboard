import { defineConfig } from 'vite'; import legacy from '@vitejs/plugin-legacy'; import injectHTML from 'vite-plugin-html-inject';
export default defineConfig({
    plugins: [
        injectHTML(),

        legacy({
            targets: ['safari >= 10'],
            polyfills: true,
            modernPolyfills: true,
        }),
    ],
    build: {
        target: 'es2015',
        minify: 'terser',
    },
    server: {
        host: true
    }
});

