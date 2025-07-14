<template>
  <div class="doc-container">
    <aside class="sidebar">
      <div
        v-for="(heading, index) in headings"
        :key="index"
        class="nav-item"
        :class="{ 'nav-level-1': heading.level === 1 }"
        :style="{ paddingLeft: (heading.level - 1) * 16 + 'px' }"
        @click="scrollToHeading(index)"
      >
        {{ heading.text }}
      </div>
    </aside>

    <main class="content" ref="contentRef">
      <div class="toolbar">
        <button @click="toggleEdit" class="toggle-btn">
          {{ isEdit ? '预览模式' : '编辑模式' }}
        </button>
        <button v-if="isEdit" @click="saveToServer" class="save-btn">保存</button>
      </div>

      <textarea
        v-if="isEdit"
        v-model="rawContent"
        class="editor"
        spellcheck="false"
      ></textarea>

      <div v-else class="preview" ref="previewRef" v-html="renderedHtml"></div>
    </main>
  </div>
</template>

<script setup>
import { ref, watch, onMounted, nextTick } from 'vue'
import { marked } from 'marked'

const rawContent = ref('')
const renderedHtml = ref('')
const isEdit = ref(false)
const headings = ref([])
const previewRef = ref(null)

async function parseMarkdown(markdown) {
  renderedHtml.value = marked.parse(markdown)
  const tokens = marked.lexer(markdown)
  headings.value = tokens
    .filter(t => t.type === 'heading')
    .map(t => ({ text: t.text, level: t.depth }))

  await nextTick()
}

onMounted(async () => {
  try {
    const res = await fetch('/doc.md')
    if (!res.ok) throw new Error('读取失败: ' + res.status)
    const text = await res.text()
    rawContent.value = text
    parseMarkdown(text)
  } catch (err) {
    console.error('❌ 加载出错:', err)
    rawContent.value = '# 加载失败\n\n' + err.message
  }
})

function toggleEdit() {
  isEdit.value = !isEdit.value
  if (!isEdit.value) parseMarkdown(rawContent.value)
}

watch(rawContent, val => {
  if (!isEdit.value) parseMarkdown(val)
})

async function saveToServer() {
  await fetch('/api/save-md', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ content: rawContent.value }),
  })
}

// 点击导航，平滑滚动到对应标题
function scrollToHeading(index) {
  const previewEl = previewRef.value
  if (!previewEl) return
  const headingEls = previewEl.querySelectorAll('h1, h2, h3, h4, h5, h6')
  const el = headingEls[index]
  if (el) {
    el.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }
}
</script>

<style scoped>
.doc-container {
  display: flex;
  height: 100vh;
  font-family: sans-serif;
}
.sidebar {
  width: 240px;
  padding: 1rem;
  background: #f4f4f4;
  border-right: 1px solid #ccc;
  overflow-y: auto;
}
.nav-item {
  font-size: 14px;
  margin-bottom: 8px;
  cursor: pointer;
}
.nav-level-1 {
  font-size: 18px;
  font-weight: bold;
  color: #007acc;
}
.content {
  flex: 1;
  padding: 1rem 2rem;
  overflow-y: auto;
}
.toolbar {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-bottom: 10px;
}
.toggle-btn,
.save-btn {
  padding: 8px 16px;
  border: none;
  border-radius: 20px;
  cursor: pointer;
}
.toggle-btn {
  background: #007acc;
  color: #fff;
}
.save-btn {
  background: #28a745;
  color: #fff;
}
.editor {
  width: 100%;
  min-height: 300px;
  font-family: monospace;
  padding: 12px;
  border-radius: 8px;
  border: 1px solid #ccc;
}
.preview {
  line-height: 1.6;
  font-size: 16px;
}
</style>
