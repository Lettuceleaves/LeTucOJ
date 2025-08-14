<script setup>
import MonacoEditor from 'monaco-editor-vue3'
import { ref, watch, nextTick } from 'vue'
import { defineEmits } from 'vue'

const props = defineProps({ editorReady: Boolean })
const emit = defineEmits(['exit', 'submit', 'check'])
const editorRef = ref(null)

const setCode = (code) => {
  if (editorRef.value?.editor) {
    editorRef.value.editor.setValue(code)
  }
}
const getCode = () => editorRef.value?.editor?.getValue() || ''

let disposeKeyDown = null

watch(() => props.editorReady, async (ready) => {
  if (ready) {
    await nextTick()
    if (disposeKeyDown) {
      disposeKeyDown.dispose()
      disposeKeyDown = null
    }
    if (editorRef.value?.editor) {
      disposeKeyDown = editorRef.value.editor.onKeyDown((e) => {
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
  }
})

defineExpose({ getCode, setCode })
</script>

<template>
  <div style="height: calc(100vh - 60px); width: 100%; position: relative;">
    <MonacoEditor
      v-if="editorReady"
      ref="editorRef"
      language="javascript"
      height="100%"
      :options="{ automaticLayout: true }"
    />
    
    <div class="floating-buttons">
      <button @click="$emit('exit')">退出</button>
      <button @click="$emit('submit')">提交</button>
      <button @click="$emit('check')">检测</button>
    </div>
  </div>
</template>

<style scoped>
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
