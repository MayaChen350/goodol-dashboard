import { defineConfig } from 'vite'; import legacy from '@vitejs/plugin-legacy'; 
import injectHTML from 'vite-plugin-html-inject';
import { dirname, resolve } from 'node:path'

export default defineConfig({
    plugins: [
        injectHTML(),

        legacy({
            targets: ['safari >= 10'],
            polyfills: true
        }),
    ],
    build: {
        rolldownOptions: {
            input: {
                main: resolve(import.meta.dirname, 'index.html'),
                weeklyTasking: resolve(import.meta.dirname, 'weeklyTasking/index.html'),
            },
        },
        target: 'es2015',
        minify: 'terser',
    },
    server: {
        host: true
    }
});

