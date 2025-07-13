<template>
  <div class="form-wrapper">
    <h2>{{ isEdit ? '编辑题目' : '新建题目' }}</h2>

    <form @submit.prevent="handleSubmit">
      <div class="grid-container">

        <div
          class="form-item"
          v-for="key in simpleFields"
          :key="key"
        >
          <label :for="key">{{ labels[key] || key }}</label>
          <input
            v-if="!isMultiline(key)"
            :id="key"
            v-model="form[key]"
            :type="inputType(key)"
          />
          <textarea
            v-else
            :id="key"
            v-model="form[key]"
            rows="4"
          />
        </div>

        <!-- 题目描述输入区域 -->
        <div class="form-item full">
          <label for="content">题目描述（可编辑）</label>
          <textarea
            id="content"
            v-model="form.content"
            rows="10"
            placeholder="请输入题目描述 Markdown"
          />
        </div>

        <!-- content 用 Markdown 渲染（只读） -->
        <div class="form-item full">
          <label>题目描述（Markdown 渲染）</label>
          <div class="markdown-preview" v-html="renderedMarkdown" />
        </div>

        <!-- solution 用 Monaco 编辑器 -->
        <div class="form-item full">
          <label>题解（可编辑）</label>
          <div class="monaco-wrapper">
            <MonacoEditor
              language="markdown"
              :value="form.solution"
              @update:value="form.solution = $event"
              :options="{ automaticLayout: true }"
            />
          </div>
        </div>
      </div>

      <div class="form-actions">
        <button type="submit">{{ isEdit ? '更新题目' : '添加题目' }}</button>
      </div>
    </form>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { marked } from 'marked'
import MonacoEditor from 'monaco-editor-vue3'

// 获取路由参数，用于判定是否是编辑
const route = useRoute()
const name = route.query.name || null
const isEdit = computed(() => !!name)

// 渲染 Markdown
const renderedMarkdown = computed(() => marked.parse(form.value.content || ''))

const hiddenFields = ['content', 'solution', 'freq', 'createTime', 'updateAt']
const simpleFields = computed(() =>
  Object.keys(form.value).filter(key => !hiddenFields.includes(key))
)

const form = ref({
  name: '',
  cnname: '',
  caseAmount: 0,
  difficulty: 1,
  tags: '',
  authors: '',
  createTime: '',
  updateAt: '',
  content: '',
  freq: 0.0,
  isPublic: 1,
  solution: '',
  showSolution: 1,
})

const labels = {
  name: '英文标识',
  cnname: '题目名称',
  caseAmount: '测试点数量',
  difficulty: '难度',
  tags: '标签',
  authors: '作者',
  createTime: '创建时间',
  updateAt: '更新时间',
  content: '题目描述',
  freq: '使用频率',
  isPublic: '是否公开',
  solution: '题解',
  showSolution: '是否展示题解',
}

const isMultiline = (key) => ['content', 'solution'].includes(key)
const inputType = (key) => {
  if (key.includes('Time')) return 'datetime-local'
  if (typeof form.value[key] === 'number') return 'number'
  return 'text'
}

onMounted(async () => {
  if (isEdit.value) {
    try {
      const response = await fetch('http://127.0.0.1:2222/practice/fullinfo', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          type: 'SELECT',
          subType: 'SINGLE_LINE',
          name,
          limit: 1,
          data: {},
        }),
      })

      const data = await response.json()
      Object.assign(form.value, data.data)
    } catch (error) {
      alert('加载失败: ' + error.message)
    }
  }
})

const handleSubmit = async () => {
  const payload = { ...form.value }

  const requestBody = {
    type: isEdit.value ? 'UPDATE' : 'INSERT',
    subType: 'SINGLE_LINE',
    name: isEdit.value ? name : payload.name,
    data: payload,
  }

  try {
    const response = await fetch('http://127.0.0.1:2222/practice/fullinfo', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(requestBody),
    })

    const res = await response.json()
    if (res.status === 0) {
      alert('提交成功')
    } else {
      alert('提交失败: ' + res.message)
    }
  } catch (err) {
    alert('提交出错: ' + err.message)
  }
}
</script>

<style scoped>
.monaco-wrapper {
  height: 30vh;
  min-height: 200px;
  border: 1px solid #ccc;
  border-radius: 6px;
  overflow: hidden;
}

.monaco-editor,
.monaco-editor-background,
.monaco-scrollable-element {
  height: 100% !important;
}

.form-wrapper {
  width: 96%;
  height: 90vh;
  max-width: 2000px;
  margin: 20px auto;
  padding: 24px;
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.05);
}

h2 {
  margin-bottom: 20px;
  font-size: 24px;
  text-align: center;
}

form {
  display: flex;
  flex-direction: column;
}

.grid-container {
  display: grid;
  grid-template-columns: repeat(4, 1fr); /* 固定 4 列 */
  gap: 16px;
}

.form-item {
  display: flex;
  flex-direction: column;
}

.form-item.full {
  grid-column: 1 / -1;
}

label {
  font-weight: bold;
  margin-bottom: 6px;
}

input,
textarea {
  width: 100%;
  padding: 8px;
  font-size: 14px;
  border: 1px solid #ccc;
  border-radius: 6px;
  box-sizing: border-box;
  resize: vertical;
}

textarea {
  min-height: 140px;
  height: 30vh;
}

.form-actions {
  margin-top: 24px;
  text-align: center;
}

button {
  padding: 10px 20px;
  background: #3b82f6;
  color: white;
  font-weight: bold;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: background-color 0.2s;
}

button:hover {
  background: #2563eb;
}

.markdown-preview {
  margin-top: 12px;
  padding: 12px;
  background-color: #f9fafb;
  border: 1px dashed #ccc;
  border-radius: 6px;
  font-size: 14px;
  line-height: 1.6;
  color: #333;
  max-height: 40vh;
  overflow-y: auto;
}
.markdown-preview h1 {
  font-size: 20px;
  margin-top: 12px;
}
.markdown-preview h2 {
  font-size: 18px;
  margin-top: 10px;
}
.markdown-preview pre {
  background-color: #eee;
  padding: 10px;
  overflow: auto;
  border-radius: 4px;
}
</style>
