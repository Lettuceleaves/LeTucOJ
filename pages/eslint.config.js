import { defineConfig, globalIgnores } from 'eslint/config'
import globals from 'globals'
import ts from '@eslint/ts'
import pluginVue from 'eslint-plugin-vue'
import skipFormatting from '@vue/eslint-config-prettier/skip-formatting'

export default defineConfig([
  {
    name: 'app/files-to-lint',
    files: ['**/*.{js,ts,mjs,jsx,vue}'],
  },

  globalIgnores(['**/dist/**', '**/dist-ssr/**', '**/coverage/**']),

  {
    languageOptions: {
      globals: {
        ...globals.browser,
      },
    },
  },

  ts.configs.recommended,
  ...pluginVue.configs['flat/essential'],
  skipFormatting,
])
