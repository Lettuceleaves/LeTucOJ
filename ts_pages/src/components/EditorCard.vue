<template>
  <el-card class="flex-1" body-class="h-full p-0">
    <vue-monaco-editor class="h-full" v-model:value="code" theme="vs-dark" :language="language" :options="editorOptions" />
  </el-card>
</template>

<script setup lang="ts">
import { VueMonacoEditor } from '@guolao/vue-monaco-editor';

const editorOptions = {
  automaticLayout: true,
  fontSize: 18,
  fontFamily: 'var(--el-font-family-monospace)',
  fontLigatures: true,

  suggestOnTriggerCharacters: true,
  quickSuggestions: true,

  formatOnPaste: true,
  formatOnType: true
};

const code = defineModel<string>('code', { default: '' });
const { language } = defineProps<{
  language: string;
}>();
</script>
