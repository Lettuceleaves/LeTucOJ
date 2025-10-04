<template>
  <div class="form-wrapper">
    <h2>{{ isEdit ? '编辑题目' : '新建题目' }}</h2>

    <form @submit.prevent="handleSubmit">
      <div class="grid-container">
        <!-- 基础字段 -->
        <div class="form-item">
          <label for="name">{{ labels.name }}</label>
          <input id="name" v-model="form.name" type="text" :readonly="isEdit" />
        </div>

        <div class="form-item">
          <label for="cnname">{{ labels.cnname }}</label>
          <input id="cnname" v-model="form.cnname" type="text" />
        </div>

        <div class="form-item">
          <label for="caseAmount">{{ labels.caseAmount }}</label>
          <input id="caseAmount" v-model="form.caseAmount" type="number" readonly />
        </div>

        <div class="form-item">
          <label for="difficulty">{{ labels.difficulty }}</label>
          <input id="difficulty" v-model.number="form.difficulty" type="number" />
        </div>

        <div class="form-item">
          <label for="tags">{{ labels.tags }}</label>
          <input id="tags" v-model="form.tags" type="text" />
        </div>

        <div class="form-item">
          <label for="authors">{{ labels.authors }}</label>
          <input id="authors" v-model="form.authors" type="text" />
        </div>

        <div class="form-item">
          <label for="publicProblem">是否公开</label>
          <select v-model.number="form.publicProblem" id="publicProblem">
            <option :value="1">是</option>
            <option :value="0">否</option>
          </select>
        </div>

        <div class="form-item">
          <label for="showsolution">是否展示题解</label>
          <select v-model.number="form.showsolution" id="showsolution">
            <option :value="1">是</option>
            <option :value="0">否</option>
          </select>
        </div>

        <!-- 题目描述 -->
        <div class="form-item full">
          <label for="content">题目描述（Markdown）</label>
          <textarea id="content" v-model="form.content" rows="10" placeholder="请输入题目描述 Markdown"></textarea>
        </div>

        <!-- 实时预览 -->
        <div class="form-item full">
          <label>题目描述（预览）</label>
          <div class="markdown-preview" v-html="renderedMarkdown"></div>
        </div>

        <!-- 题解 Monaco -->
        <div class="form-item full">
          <label>题解（可编辑）</label>
          <div ref="editorDom" class="monaco-target"></div>
        </div>
      </div>

      <div class="form-actions">
        <button type="submit">{{ isEdit ? '更新题目' : '添加题目' }}</button>
      </div>

      <!-- 输入输出区块 -->
      <div class="form-item full">
        <label>输入输出</label>
        <div
          class="input-output-box"
          v-for="(item, index) in inputOutputSections"
          :key="index"
        >
          <div class="input-output-content">
            <textarea
              v-model="item.input"
              placeholder="输入内容"
              @input="adjustHeight($event, index, 'input')"
            ></textarea>
            <textarea
              v-model="item.output"
              placeholder="输出内容"
              :ref="el => outputRefs[index] = el"
              disabled
            ></textarea>
          </div>
          <div class="input-output-actions">
            <button type="button" @click="submitCase(index)">提交</button>
            <button type="button" @click="getOutput(index)">获取输出</button>
          </div>
        </div>
      </div>
    </form>

    <div class="add-section-btn" @click="addInputOutputSection">
      <span>+</span>
    </div>
  </div>
</template>
<script setup>
import {
  ref,
  computed,
  onMounted,
  onUnmounted,
  nextTick,
  getCurrentInstance,
} from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { marked } from 'marked'
import * as monaco from 'monaco-editor'
import { setupMyC } from '../components/monaco-c'
import 'monaco-editor/min/vs/editor/editor.main.css'

/* -------------------------------------------------- */
/* 1. 路由 & 全局属性                                    */
/* -------------------------------------------------- */
const route = useRoute()
const router = useRouter()
const isEdit = computed(() => !!route.query.name)
const instance = getCurrentInstance()
const ip = instance.appContext.config.globalProperties.$ip

/* -------------------------------------------------- */
/* 2. 表单数据                                           */
/* -------------------------------------------------- */
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

/* -------------------------------------------------- */
/* 3. Markdown 预览                                      */
/* -------------------------------------------------- */
const renderedMarkdown = computed(() => marked.parse(form.value.content || ''))

/* -------------------------------------------------- */
/* 4. Monaco 编辑器（去重版）                             */
/* -------------------------------------------------- */
const editorDom = ref(null)
let editor = null
let disposer = null

function createEditor() {
  if (editor) return

  setupMyC(monaco)          // ② 注册语言、主题、补全

  editor = monaco.editor.create(editorDom.value, {
    value: form.value.solution,
    language: 'myC',        // ③ 使用自定义语言
    theme: 'myCTheme',      // ④ 使用自定义主题
    automaticLayout: true,
    minimap: { enabled: false },
    fontSize: 15,
    scrollBeyondLastLine: false,
    suggestOnTriggerCharacters: true,
    quickSuggestions: true,
  })

  // 双向绑定
  disposer = editor.onDidChangeModelContent(() => {
    form.value.solution = editor.getValue()
  })
}

function disposeEditor() {
  disposer?.dispose()
  editor?.dispose()
  editor = disposer = null
}

onMounted(async () => {
  if (isEdit.value) {
    try {
      const token = localStorage.getItem('jwt')
      const params = new URLSearchParams({ qname: route.query.name })
      const res = await fetch(`http://${ip}/practice/full/get?${params}`, {
        headers: { Authorization: `Bearer ${token}` },
      })
      const data = await res.json()
      Object.assign(form.value, data.data)
    } catch (e) {
      alert('加载失败: ' + e.message)
    }
  }
  await nextTick()
  createEditor()
})

onUnmounted(disposeEditor)

/* -------------------------------------------------- */
/* 5. 提交 / 更新                                         */
/* -------------------------------------------------- */
const handleSubmit = () => (isEdit.value ? updateProblem() : addProblem())

async function addProblem() {
  try {
    const token = localStorage.getItem('jwt')
    const res = await fetch(`http://${ip}/practice/fullRoot/insert`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${token}`,
      },
      body: JSON.stringify(form.value),
    }).then((r) => r.json())
    if (res.code === "0") {
      alert('添加成功')
      router.replace({ query: { name: form.value.name } })
    } else {
      alert('添加失败: ' + JSON.stringify(res))
    }
  } catch (e) {
    alert('添加出错: ' + e.message)
  }
}

async function updateProblem() {
  try {
    const token = localStorage.getItem('jwt')
    const res = await fetch(`http://${ip}/practice/fullRoot/update`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${token}`,
      },
      body: JSON.stringify(form.value),
    }).then((r) => r.json())
    if (res.code === "0") {
      alert('更新成功')
    } else {
      alert('更新失败: ' + JSON.stringify(res))
    }
  } catch (e) {
    alert('更新出错: ' + e.message)
  }
}

/* -------------------------------------------------- */
/* 6. 输入输出区块（无改动）                              */
/* -------------------------------------------------- */
const inputOutputSections = ref([{ input: '', output: '' }])
const outputRefs = ref([])

const addInputOutputSection = () => {
  inputOutputSections.value.push({ input: '', output: '' })
}

const adjustHeight = (e) => {
  const t = e.target
  t.style.height = 'auto'
  t.style.height = t.scrollHeight + 'px'
}
const adjustHeightForOutput = (idx) =>
  nextTick(() => adjustHeight({ target: outputRefs.value[idx] }))

async function submitCase(index) {
  if (inputOutputSections.value[index].output === '已提交') {
    alert('不要重复提交')
    return
  }
  if (!inputOutputSections.value[index].input || !inputOutputSections.value[index].output) {
    alert('请先获取输出')
    return
  }
  try {
    const token = localStorage.getItem('jwt')
    const res = await fetch(`http://${ip}/practice/submitCase`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${token}`,
      },
      body: JSON.stringify({
        name: form.value.name,
        input: inputOutputSections.value[index].input,
        output: inputOutputSections.value[index].output,
      }),
    }).then((r) => r.json())
    if (res.code === "0") {
      inputOutputSections.value[index].output = '已提交'
      await nextTick()
      adjustHeightForOutput(index)
      form.value.caseAmount += 1
      alert('提交成功')
    } else {
      alert('提交失败: ' + res.error)
    }
  } catch (e) {
    alert('提交出错: ' + e.message)
  }
}

async function getOutput(index) {
  if (inputOutputSections.value[index].output === '已提交') {
    alert('不要获取已提交的案例')
    return
  }
  if (!inputOutputSections.value[index].input) {
    alert('请输入内容')
    return
  }
  try {
    const token = localStorage.getItem('jwt')
    const res = await fetch(`http://${ip}/practice/getCase`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${token}`,
      },
      body: JSON.stringify({
        input: inputOutputSections.value[index].input,
        code: form.value.solution,
        name: form.value.name
      }),
    }).then((r) => r.json())
    if (res.code === "0") {
      const out = Array.isArray(res.data) ? res.data[0] : res.data
      inputOutputSections.value[index].output = out
      await nextTick()
      adjustHeightForOutput(index)
    } else {
      alert('获取输出失败: ' + res.message)
    }
  } catch (e) {
    alert('获取输出出错: ' + e.message)
  }
}
</script>

<style scoped>
/* 编辑器容器 */
.monaco-target {
  width: 100%;
  height: 30vh;
  min-height: 600px;
  border: 1px solid #ccc;
  border-radius: 6px;
  overflow: hidden;
}

/* 其余样式与原文件完全一致 */
.form-wrapper {
  width: 96%;
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
.grid-container {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
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
select,
textarea {
  width: 100%;
  padding: 8px;
  font-size: 14px;
  border: 1px solid #ccc;
  border-radius: 6px;
  box-sizing: border-box;
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
