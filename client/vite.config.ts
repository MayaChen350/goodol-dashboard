import { defineConfig, UserConfig } from 'vite';
import legacy from '@vitejs/plugin-legacy';
import injectHTML from 'vite-plugin-html-inject';
import { resolve } from 'node:path';

import postcssPreset from 'postcss-preset-env';
import postCssImport from 'postcss-import';
import tailwindcss from 'tailwindcss';

export default defineConfig({
    plugins: [
        injectHTML(),

        legacy({
            targets: ['defaults', 'ios_saf 10'],
            polyfills: true
        }),
    ],
    build: {
        rolldownOptions: {
            input: {
                main: resolve(__dirname, 'index.html'),
                weeklyTasking: resolve(__dirname, 'weeklyTasking/index.html'),
            },
        },
        cssCodeSplit: false,
        minify: 'terser',
    },
    server: {
        host: true
    },
    css: {
        transformer: 'postcss',
        postcss: {
            plugins: [
                postCssImport(),
                tailwindcss(),
                postcssPreset({
                    stage: 2,
                    autoprefixer: {
                        cascade: false
                    }
                })
            ]
        }
    }
} as UserConfig);

