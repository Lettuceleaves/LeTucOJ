<template>
  <div class="editor-wrap">
    <!-- 顶部导航栏 -->
    <nav class="top-bar">
      <div class="left-area">
        <button class="bar-btn" @click="$emit('exit')">退出</button>
        <button class="bar-btn" @click="$emit('submit')">提交</button>
        <!-- <button class="bar-btn" @click="$emit('check')">检测</button> -->
      </div>

      <select v-model="selectedLanguage" class="language-select">
        <option value="c">C</option>
        <option value="cpp">C++</option>
        <option value="node">Node.js</option>
        <option value="py">Python</option>
        <option value="java">Java</option>
      </select>
    </nav>

    <!-- Monaco 容器 -->
    <div ref="el" class="monaco-target"></div>
  </div>
</template>

<script setup>
/* 下方 <script> 完全沿用你给出的代码，无需任何改动 */
import { ref, onMounted, onUnmounted, watch, nextTick, inject, computed } from 'vue'
import * as monaco from 'monaco-editor'
import { setupMyC } from '../../components/monaco-c'

const lang    = inject('lang')
const setLang = inject('setLang')
const selectedLanguage = computed({
  get: () => lang.value,
  set: val => setLang(val)
})

const props = defineProps({ editorReady: Boolean })
const emit  = defineEmits(['exit', 'submit', 'check'])

const el = ref()
let ed = null
let dis = null

const getCode = () => ed?.getValue() || ''
const setCode = (userCode) => ed?.setValue(userCode)
defineExpose({ getCode, setCode })

function create() {
  if (ed) return
  setupMyC(monaco)
  ed = monaco.editor.create(el.value, {
    value: `#include <stdio.h>\n\nint main() {\n    printf("Hello,World");\n}`,
    language: 'myC',
    theme: 'myCTheme',
    automaticLayout: true,
    minimap: { enabled: false },
    autoClosingBrackets: 'always',
    autoClosingQuotes: 'always',
    autoSurround: 'languageDefined',
    formatOnType: true,
    formatOnPaste: true,
    autoIndent: 'full',
    suggestOnTriggerCharacters: true,
    quickSuggestions: true
  })
  dis = ed.onKeyDown(e => {
    if (e.browserEvent.key === ' ' || e.browserEvent.key === 'Enter') {
      localStorage.setItem('userCode', getCode())
    }
  })
}

onMounted(() => { if (props.editorReady) create() })
onUnmounted(() => { dis?.dispose(); ed?.dispose() })
watch(() => props.editorReady, async r => { if (r) { await nextTick(); create() } }, { immediate: true })
</script>

<style scoped>
/* 布局：顶部栏 48px，下方 Monaco 占满剩余高度 */
.editor-wrap {
  height: 100vh;
  display: flex;
  flex-direction: column;
}
.top-bar {
  height: 48px;
  background: #d4efff;
  border-bottom: 1px solid #dbe2ff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  flex-shrink: 0;
}
.left-area {
  display: flex;
  gap: 8px;
}
.bar-btn {
  padding: 6px 12px;
  background: #3b82f6;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}
.bar-btn:hover {
  background: #2563eb;
}
.language-select {
  padding: 4px 8px;
  font-size: 14px;
  border-radius: 4px;
  border: 1px solid #ffffff;
  background: #b9cfff;
  color: #151955;
  cursor: pointer;
}
.monaco-target {
  flex: 1;               /* 占满剩余空间 */
  width: 100%;
}
</style>
