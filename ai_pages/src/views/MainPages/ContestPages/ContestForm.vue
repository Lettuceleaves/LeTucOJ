<template>
  <div class="form-wrapper">
    <h2>{{ isEdit ? '编辑竞赛' : '新建竞赛' }}</h2>

    <form @submit.prevent="handleSubmit">
      <div class="grid-container">
        <!-- name -->
        <div class="form-item">
          <label for="data-name">{{ labels.name }}</label>
          <input id="name" v-model="form.name" type="text" :readonly="isEdit" />
        </div>

        <!-- cnname -->
        <div class="form-item">
          <label for="cnname">{{ labels.cnname }}</label>
          <input id="nname" v-model="form.cnname" type="text" />
        </div>

        <!-- mode -->
        <div class="form-item">
          <label for="mode">{{ labels.mode }}</label>
          <select id="mode" v-model="form.mode">
            <option value="add">添加</option>
            <option value="all">全部</option>
          </select>
        </div>

        <!-- start -->
        <div class="form-item">
          <label for="start">{{ labels.start }}</label>
          <input id="start" v-model="form.start" type="datetime-local" />
        </div>

        <!-- end -->
        <div class="form-item">
          <label for="end">{{ labels.end }}</label>
          <input id="end" v-model="form.end" type="datetime-local" />
        </div>

        <!-- ispublic -->
        <div class="form-item">
          <label>是否公开</label>
          <div style="display: flex; gap: 12px; align-items: center">
            <label style="font-weight: normal; margin: 0">
              <input type="radio" :value="true" v-model="form.publicContest" />
              是
            </label>
            <label style="font-weight: normal; margin: 0">
              <input type="radio" :value="false" v-model="form.publicContest" />
              否
            </label>
          </div>
        </div>

        <!-- note -->
        <div class="form-item full">
          <label for="note">{{ labels.note }}</label>
          <textarea id="note" v-model="form.note" rows="10" placeholder="支持 Markdown 格式" />
        </div>

        <!-- note Markdown 渲染 -->
        <div class="form-item full">
          <label style="width: auto">备注预览</label>
          <div class="markdown-preview" v-html="renderedNote" />
        </div>
      </div>

      <div class="form-actions">
        <button type="submit" @click="returnCallBack">
          {{ isEdit ? '更新竞赛' : '添加竞赛' }}
        </button>
      </div>
    </form>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, getCurrentInstance } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { marked } from 'marked'

const route = useRoute()
const router = useRouter()

const instance = getCurrentInstance()

const REMOTE_SERVER_URL = import.meta.env.VITE_REMOTE_SERVER_URL

const isEdit = computed(() => !!route.query.ctname)

// 初始化表单数据
const initialForm = {
  name: '',
  cnname: '',
  mode: 'add', // 默认值改为 "add"
  start: '',
  end: '',
  publicContest: true, // 直接使用布尔值
  note: '',
}

const form = ref({ ...initialForm })

const labels = {
  name: '英文名称',
  cnname: '中文名称',
  mode: '竞赛模式',
  start: '开始时间',
  end: '结束时间',
  publicContest: '是否公开', // 保持一致的命名方式
  note: '备注信息',
}

const renderedNote = computed(() => marked.parse(form.value.note || ''))

const fetchCompetitionData = async () => {
  if (!isEdit.value) return
  try {
    const token = localStorage.getItem('jwt')
    const params = new URLSearchParams({ ctname: route.query.ctname })

    const res = await fetch(`${REMOTE_SERVER_URL}/contest/full/getContest?${params}`, {
      method: 'GET',
      headers: { Authorization: `Bearer ${token}` },
    })
    if (!res.ok) throw new Error('获取竞赛数据失败')
    const data = await res.json()
    if (data.status !== 0 || !data.data) {
      throw new Error(data.error || '竞赛数据格式异常')
    }
    Object.assign(form.value, data.data)
    // 确保 boolean 类型正确
    form.value.publicContest = Boolean(data.data.publicContest)
  } catch (error) {
    alert('获取竞赛数据失败: ' + error.message)
  }
}

const handleSubmit = () => {
  isEdit.value ? updateCompetition() : addCompetition()
}

const handleFormSubmit = async (method, endpoint) => {
  try {
    const token = localStorage.getItem('jwt')
    const payload = {
      name: form.value.name,
      cnname: form.value.cnname,
      mode: form.value.mode,
      start: form.value.start,
      end: form.value.end,
      publicContest: Boolean(form.value.publicContest),
      note: form.value.note,
    }

    const res = await fetch(`${REMOTE_SERVER_URL}/${endpoint}`, {
      method: method,
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${token}`,
      },
      body: JSON.stringify(payload),
    })

    if (!res.ok) throw new Error('操作失败')
    const data = await res.json()

    if (data.status === 0) {
      if (isEdit.value) {
        alert('竞赛更新成功')
      } else {
        alert('竞赛添加成功')
        form.value = { ...initialForm } // 重置表单
      }
    } else {
      alert(`操作失败: ${data.error}`)
    }
  } catch (error) {
    alert('操作出错: ' + error.message)
  }
}

// 表单验证函数
const validateForm = () => {
  if (!form.value.name) {
    alert('请输入英文名称')
    return false
  }
  if (!form.value.cnname) {
    alert('请输入中文名称')
    return false
  }
  if (!form.value.start) {
    alert('请选择开始时间')
    return false
  }
  if (!form.value.end) {
    alert('请选择结束时间')
    return false
  }
  if (new Date(form.value.start) >= new Date(form.value.end)) {
    alert('结束时间必须晚于开始时间')
    return false
  }
  return true
}

const addCompetition = () => {
  if (!validateForm()) return
  handleFormSubmit('POST', 'contest/insertContest')
}

const updateCompetition = () => {
  if (!validateForm()) return
  handleFormSubmit('PUT', 'contest/updateContest')
}

onMounted(() => {
  fetchCompetitionData()
})

function returnCallBack() {
  router.push('/main')
}
</script>

<style scoped>
/* 样式保持不变 */
.form-wrapper {
  width: 96%;
  max-width: 1000px;
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
}

textarea {
  min-height: 200px;
  resize: vertical;
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
