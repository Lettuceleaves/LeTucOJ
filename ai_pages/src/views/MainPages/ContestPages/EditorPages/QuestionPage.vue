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
    <div ref="editorDom" class="monaco-target"></div>
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
  inject, 
  computed
} from 'vue'
import * as monaco from 'monaco-editor'
import { setupMyC } from '../../../../components/monaco-c'
import 'monaco-editor/min/vs/editor/editor.main.css'

const lang = inject('lang') 
const setLang = inject('setLang')

const selectedLanguage = computed({
  get: () => lang.value,  // 当 select 读取值时，返回父组件的状态
  set: val => setLang(val) // 当 select 写入新值时，调用父组件的方法来更新状态
})

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

  setupMyC(monaco)

  editor = monaco.editor.create(editorDom.value, {
    value:
      localStorage.getItem('userCode') ||
      '#include <stdio.h>\n\nint main() {\n    \n    return 0;\n}',
    language: 'myC',
    theme: 'myCTheme',
    automaticLayout: true,
    minimap: { enabled: false },
    fontSize: 15,
    scrollBeyondLastLine: false,
    suggestOnTriggerCharacters: true,
    quickSuggestions: true,
  })

  // 自动保存
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
