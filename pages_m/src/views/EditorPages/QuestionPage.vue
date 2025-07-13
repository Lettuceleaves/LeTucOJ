<template>
  <div class="question-panel">
    <div :style="{ height: 'calc(100vh - 60px)', width: '99%' }">
      <MonacoEditor
        v-if="editorReady"
        ref="editorRef"
        language="javascript"
        height="100%"
        :options="{ automaticLayout: true }"
      />
    </div>

    <div class="floating-buttons">
      <button @click="$emit('exit')">退出</button>
      <button @click="$emit('submit')">提交</button>
      <button @click="$emit('check')">检测</button>
    </div>
  </div>
</template>

<script setup>
import MonacoEditor from 'monaco-editor-vue3'
import { ref, defineExpose } from 'vue'

const props = defineProps({ editorReady: Boolean })
const editorRef = ref(null)

// 暴露给父组件调用获取代码的方法
defineExpose({
  getCode: () => editorRef.value?.editor?.getValue() || '',
})
</script>

<style>
.question-panel {
  position: relative;
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
