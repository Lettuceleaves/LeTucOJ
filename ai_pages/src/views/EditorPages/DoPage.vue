<template>
  <div class="do-page">
    <!-- 左：描述 -->
    <div class="left-pane" :style="{ width: leftWidth + '%' }">
      <DescriptionPage :data="problemData" />
    </div>

    <!-- 拖拽条 -->
    <div class="divider" @mousedown="startDragging"></div>

    <!-- 右：答题 -->
    <div class="right-pane" :style="{ width: 100 - leftWidth + '%' }">
      <QuestionPage
        :editorReady="editorReady"
        @exit="$emit('exit')"
        @submit="$emit('submit')"
        @check="$emit('check')"
        ref="qRef"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import DescriptionPage from './DescriptionPage.vue'
import QuestionPage from './QuestionPage.vue'

defineProps({
  editorReady: Boolean,
  problemData: Object
})
defineEmits(['exit','submit','check'])

const qRef = ref()
defineExpose({
  getCode: () => qRef.value?.getCode?.(),
  setCode: c => qRef.value?.setCode?.(c)
})

// 左侧宽度百分比
const leftWidth = ref(50)
let isDragging = false
const STORAGE_KEY = 'doPage-leftWidth'

const startDragging = () => {
  isDragging = true
  document.body.style.cursor = 'col-resize'
}

const stopDragging = () => {
  if (isDragging) {
    localStorage.setItem(STORAGE_KEY, leftWidth.value.toFixed(2)) // 存储精确两位小数
  }
  isDragging = false
  document.body.style.cursor = 'default'
}

const onDrag = (e) => {
  if (!isDragging) return
  const container = document.querySelector('.do-page')
  const rect = container.getBoundingClientRect()
  const percent = ((e.clientX - rect.left) / rect.width) * 100
  // 限制范围：20% ~ 80%
  if (percent > 20 && percent < 80) {
    leftWidth.value = percent
  }
}

onMounted(() => {
  // 初始化时从 localStorage 读取
  const saved = localStorage.getItem(STORAGE_KEY)
  if (saved) {
    leftWidth.value = parseFloat(saved)
  }

  window.addEventListener('mousemove', onDrag)
  window.addEventListener('mouseup', stopDragging)
})
onBeforeUnmount(() => {
  window.removeEventListener('mousemove', onDrag)
  window.removeEventListener('mouseup', stopDragging)
})
</script>

<style scoped>
.do-page {
  display: flex;
  height: 100%;
  width: 100%;
  overflow: hidden;
}

.left-pane,
.right-pane {
  height: 100%;
  overflow: auto;
}

.divider {
  width: 5px;
  cursor: col-resize;
  background: #ddd;
  transition: background 0.2s;
}
.divider:hover {
  background: #bbb;
}
</style>
