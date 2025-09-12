<template>
  <div class="editor-wrap">
    <div ref="el" class="monaco-target"></div>

    <div class="floating-buttons">
      <button @click="$emit('exit')">退出</button>
      <button @click="$emit('submit')">提交</button>
      <button @click="$emit('check')">检测</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import * as monaco from 'monaco-editor'   // 直接整体引入
import 'monaco-editor/min/vs/editor/editor.main.css'

const props = defineProps({ editorReady: Boolean })
const emit = defineEmits(['exit','submit','check'])

const el = ref()
let ed = null
let dis = null

const getCode = () => ed?.getValue() || ''
const setCode = (code) => ed?.setValue(code)
defineExpose({ getCode, setCode })

function create() {
  if (ed) return
  ed = monaco.editor.create(el.value, {
    value: localStorage.getItem('userCode') || '#include <stdio.h>\n\nint main() {\n    return 0;\n}',
    language: 'c',          // 小写即可，插件已注册
    automaticLayout: true,
    minimap: { enabled: false }
  })

  dis = ed.onKeyDown(e => {
    if (e.browserEvent.key === ' ' || e.browserEvent.key === 'Enter') {
      const code = getCode()
      if (code) localStorage.setItem('userCode', code)
    }
  })
}

onMounted(() => { if (props.editorReady) create() })
onUnmounted(() => { dis?.dispose(); ed?.dispose() })
watch(() => props.editorReady, async r => { if (r) { await nextTick(); create() } }, { immediate: true })
</script>

<style scoped>
.editor-wrap { height: calc(100vh - 60px); position: relative; }
.monaco-target { width: 100%; height: 100%; }
.floating-buttons { position: absolute; top: 20px; right: 20px; display: flex; flex-direction: column; gap: 10px; z-index: 1000; }
.floating-buttons button { padding: 8px 12px; background: #3b82f6; color: #fff; border: none; border-radius: 6px; cursor: pointer; }
.floating-buttons button:hover { background: #2563eb; }
</style>
