import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import monacoPlugin from 'vite-plugin-monaco-editor'

// 取 .default（commonjs 转 esm 的坑）
const monaco = monacoPlugin.default || monacoPlugin

export default defineConfig({
  plugins: [
    vue(),
    monaco({
      languages: ['markdown'],
      publicPath: '/assets/'   // 与 build.assetsDir 保持一致
    })
  ]
})
