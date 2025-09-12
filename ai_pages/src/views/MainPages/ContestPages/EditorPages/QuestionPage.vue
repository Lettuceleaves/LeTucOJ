<template>
  <div class="editor-wrap">
    <!-- 挂载点 -->
    <div ref="editorDom" class="monaco-target"></div>

    <!-- 悬浮按钮 -->
    <div class="floating-buttons">
      <button @click="$emit('exit')">退出</button>
      <button @click="$emit('submit')">提交</button>
      <button @click="$emit('check')">检测</button>
    </div>
  </div>
</template>

<script setup>
import {
  ref,
  onMounted,
  onUnmounted,
  watch,
  nextTick,
  defineExpose,
} from 'vue'
import * as monaco from 'monaco-editor'
import 'monaco-editor/min/vs/editor/editor.main.css'

const props = defineProps({ editorReady: Boolean })
const emit  = defineEmits(['exit', 'submit', 'check'])

const editorDom = ref(null)
let editor = null
let disposer = null

/* 父组件可调用的方法 */
const getCode = () => editor?.getValue() || ''
const setCode = (code) => editor?.setValue(code)
defineExpose({ getCode, setCode })

function createEditor() {
  if (editor) return

  editor = monaco.editor.create(editorDom.value, {
    value:
      localStorage.getItem('userCode') ||
      '#include <stdio.h>\n\nint main() {\n    \n    return 0;\n}',
    language: 'c',          // 小写即可，插件已注册
    automaticLayout: true,
    minimap: { enabled: false },
    fontSize: 15,
    scrollBeyondLastLine: false,
  })

  // 自动保存（空格 / 回车）
  disposer = editor.onKeyDown((e) => {
    const key = e.browserEvent.key
    if (key === ' ' || key === 'Enter') {
      const code = getCode()
      if (code) {
        localStorage.setItem('userCode', code)
        console.log('代码保存成功，按键:', key)
      }
    }
  })
}

function disposeEditor() {
  disposer?.dispose()
  editor?.dispose()
  editor = disposer = null
}

onMounted(() => {
  if (props.editorReady) createEditor()
})

onUnmounted(disposeEditor)

watch(
  () => props.editorReady,
  async (ready) => {
    if (!ready) return
    await nextTick()
    createEditor()
  },
  { immediate: true }
)
</script>

<style scoped>
.editor-wrap {
  height: calc(100vh - 60px);
  position: relative;
}
.monaco-target {
  width: 100%;
  height: 100%;
}
.floating-buttons {
  position: absolute;
  top: 20px;
  right: 20px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  z-index: 1000;
}
.floating-buttons button {
  padding: 8px 12px;
  background-color: #3b82f6;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: bold;
  transition: background-color 0.2s;
}
.floating-buttons button:hover {
  background-color: #2563eb;
}
</style>
