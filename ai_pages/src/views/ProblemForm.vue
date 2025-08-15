<template>
  <div class="form-wrapper">
    <h2>{{ isEdit ? '编辑题目' : '新建题目' }}</h2>

    <form @submit.prevent="handleSubmit">
      <div class="grid-container">

        <!-- name -->
        <div class="form-item">
          <label for="name">{{ labels.name }}</label>
          <input 
            id="name" 
            v-model="form.name" 
            type="text" 
            :readonly="isEdit"
          />
        </div>

        <!-- cnname -->
        <div class="form-item">
          <label for="cnname">{{ labels.cnname }}</label>
          <input id="cnname" v-model="form.cnname" type="text" />
        </div>

        <!-- caseAmount -->
        <div class="form-item">
          <label for="caseAmount">{{ labels.caseAmount }}</label>
          <input id="caseAmount" v-model="form.caseAmount" type="number" :readonly="true" />
        </div>

        <!-- difficulty -->
        <div class="form-item">
          <label for="difficulty">{{ labels.difficulty }}</label>
          <input id="difficulty" v-model="form.difficulty" type="number" />
        </div>

        <!-- tags -->
        <div class="form-item">
          <label for="tags">{{ labels.tags }}</label>
          <input id="tags" v-model="form.tags" type="text" />
        </div>

        <!-- authors -->
        <div class="form-item">
          <label for="authors">{{ labels.authors }}</label>
          <input id="authors" v-model="form.authors" type="text" />
        </div>

        <!-- 替换原来的两个输入框，用下拉框选择是否公开 -->
        <div class="form-item">
          <label for="publicProblem">是否公开</label>
          <select v-model="form.publicProblem">
            <option value=true>是</option>
            <option value=false>否</option>
          </select>
        </div>

        <div class="form-item">
          <label for="showsolution">是否展示题解</label>
          <select v-model="form.showsolution">
            <option value=true>是</option>
            <option value=false>否</option>
          </select>
        </div>

        <!-- 题目描述输入区域 -->
        <div class="form-item full">
          <label for="content">题目描述（可编辑）</label>
          <textarea id="content" v-model="form.content" rows="10" placeholder="请输入题目描述 Markdown" />
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

      <div class="form-item full">
        <label>输入输出</label>
        <div class="input-output-box" v-for="(item, index) in inputOutputSections" :key="index">
          <div class="input-output-content">
            <textarea 
              v-model="item.input" 
              placeholder="请输入内容" 
              @input="adjustHeight($event, index, 'input')"
            />
            <textarea 
              v-model="item.output" 
              placeholder="输出内容" 
              ref="outputRefs"
              disabled
            />
          </div>
          <div class="input-output-actions">
            <button type="button" @click="submit(index)">提交</button>
            <button type="button" @click="getOutput(index)">获取输出</button>
          </div>
        </div>
      </div>

    </form>

    <!-- Add New Section Button -->
    <div class="add-section-btn" @click="addInputOutputSection">
      <span>+</span>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, getCurrentInstance, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { marked } from 'marked'
import MonacoEditor from 'monaco-editor-vue3'

// 获取路由参数
const route = useRoute()
const router = useRouter()

// ✅ 修复：isEdit 改为响应式
const isEdit = computed(() => !!route.query.name)

const instance = getCurrentInstance()
const ip = instance.appContext.config.globalProperties.$ip

// 渲染 Markdown
const renderedMarkdown = computed(() => marked.parse(form.value.content || ''))

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
  solution: '',
  publicProblem: 1,
  showsolution: 1,
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
  solution: '题解',
}

onMounted(async () => {
  if (isEdit.value) {
    try {
      const token = localStorage.getItem('jwt')
      const params = new URLSearchParams({ qname: route.query.name })
      const response = await fetch(`http://${ip}/practice/full/get?${params}`, {
        method: 'GET',
        headers: { Authorization: `Bearer ${token}` }
      })
      const data = await response.json()
      Object.assign(form.value, data.data)
    } catch (error) {
      alert('加载失败: ' + error.message)
    }
  }
})

const handleSubmit = () => {
  isEdit.value ? updateProblem() : addProblem()
}

const addProblem = async () => {
  try {
    const token = localStorage.getItem('jwt')
    const res = await fetch(`http://${ip}/practice/fullRoot/insert`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${token}`
      },
      body: JSON.stringify(form.value)
    }).then(r => r.json())

    if (res.status === 0) {
      alert('添加成功')
      router.replace({ query: { name: form.value.name } })
    } else {
      alert('添加失败：' + JSON.stringify(res))
    }
  } catch (e) {
    alert('添加出错：' + e.message)
  }
}

const updateProblem = async () => {
  try {
    const token = localStorage.getItem('jwt')

    const payload = { ...form.value }

    const res = await fetch(`http://${ip}/practice/fullRoot/update`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${token}`
      },
      body: JSON.stringify(payload)
    }).then(r => r.json())

    if (res.status === 0) {
      alert('更新成功')
    } else {
      alert('更新失败：' + JSON.stringify(res))
    }
  } catch (e) {
    alert('更新出错：' + e.message)
  }
}

// 输入输出区块逻辑（略，保持不变）
const inputOutputSections = ref([{ input: '', output: '' }])
const outputRefs = ref([])

const addInputOutputSection = () => {
  inputOutputSections.value.push({ input: '', output: '' })
}

const submit = async (index) => {
  if (inputOutputSections.value[index].output === '已提交') {
    alert('不要重复提交')
    return
  } else if (inputOutputSections.value[index].input && inputOutputSections.value[index].output) {
    try {
      const response = await fetch(`http://${ip}/practice/submitCase`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${localStorage.getItem('jwt')}`
        },
        body: JSON.stringify({
          name: form.value.name,
          input: inputOutputSections.value[index].input,
          output: inputOutputSections.value[index].output,
        }),
      })
      const data = await response.json()
      if (data.status === 0) {
        inputOutputSections.value[index].output = '已提交'
        await nextTick()
        adjustHeightForOutput(index)
        form.value.caseAmount += 1
        alert('提交成功')
      } else {
        alert('提交失败: ' + data.error)
      }
    } catch (error) {
      alert('提交出错: ' + error.message)
    }
  } else {
    alert('请先获取输出')
  }
}

const getOutput = async (index) => {
  if (inputOutputSections.value[index].output === '已提交') {
    alert('不要获取已提交的案例')
    return
  } else if (inputOutputSections.value[index].input) {
    try {
      const response = await fetch(`http://${ip}/practice/getCase`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${localStorage.getItem('jwt')}`
        },
        body: JSON.stringify({
          input: inputOutputSections.value[index].input,
          code: form.value.solution,
          name: form.value.name,
        }),
      })
      const data = await response.json()
      if (data.status === 0) {
        inputOutputSections.value[index].output = Array.isArray(data.data) && data.data[0]
        await nextTick()
        adjustHeightForOutput(index)
      } else {
        alert('获取输出失败: ' + data.error)
      }
    } catch (error) {
      alert('提交出错: ' + error.message)
    }
  } else {
    alert('请输入内容')
  }
}

const adjustHeightForOutput = (index) => {
  const outputTextarea = outputRefs.value[index]
  if (outputTextarea) {
    outputTextarea.style.height = 'auto'
    outputTextarea.style.height = `${outputTextarea.scrollHeight}px`
  }
}

const adjustHeight = (event, index, type) => {
  const textarea = event.target
  textarea.style.height = 'auto'
  textarea.style.height = `${textarea.scrollHeight}px`
}
</script>

<style scoped>
.monaco-wrapper {
  height: 30vh;
  min-height: 600px;
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
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.form-item {
  display: flex;
  flex-direction: column;
}

.form-item label {
  font-weight: normal;
}

select {
  padding: 8px;
  font-size: 14px;
  border: 1px solid #ccc;
  border-radius: 6px;
  width: 100%;
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
  min-height: 600px;
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

.input-output-box {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 20px;
  margin-top: 20px;
  background-color: #f7f7f7;
  padding: 16px;
  border-radius: 8px;
  border: 1px solid #ddd;
}

.input-output-content {
  display: flex;
  flex-direction: column;
  gap: 10px;
  width: 90%;
}

.input-output-content textarea {
  padding: 8px;
  font-size: 14px;
  border: 1px solid #ccc;
  border-radius: 6px;
  width: 100%;
  min-height: 10px;
  resize: vertical;
  height: auto;
  white-space: pre-wrap;
  overflow: hidden;
}

.input-output-actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
  align-items: center;
}

.input-output-actions button {
  padding: 8px 12px;
  width: 80px;
  background: #3b82f6;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
}

.input-output-actions button:hover {
  background: #2563eb;
}

.add-section-btn {
  position: relative;
  left: 50%;
  margin-top: 20px;
  width: 50px;
  height: 50px;
  background-color: #3b82f6;
  color: white;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 24px;
  cursor: pointer;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.2);
}

.add-section-btn:hover {
  background-color: #2563eb;
}
</style>
