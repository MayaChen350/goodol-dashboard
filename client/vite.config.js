import {sveltekit} from '@sveltejs/kit/vite';
import {defineConfig} from 'vite';
import legacy from '@vitejs/plugin-legacy';
import tailwindcss from "@tailwindcss/vite";

const veryOldDeviceInUse = 'ios_saf >= 10'

export default defineConfig({
    plugins: [tailwindcss(), sveltekit(),
        legacy({
            targets: ['defaults', veryOldDeviceInUse],
            renderLegacyChunks: false,
            modernPolyfills: true
        }),
    ]
});
