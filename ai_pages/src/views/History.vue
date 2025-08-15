<template>
  <div class="record-list">
    <!-- 仅 ROOT / MANAGER 可见 -->
    <div v-if="isPrivileged" class="action-bar">
      <button @click="searchByUser">查找特定用户记录</button>
      <button @click="searchAll">查找全部用户记录</button>
    </div>

    <ul class="records">
      <li v-for="r in sortedRecords" :key="r.submitTime" class="record">
        <!-- 基本信息 -->
        <div class="row"><span class="label">用户：</span>{{ r.userName }}({{ r.cnname }})</div>
        <div class="row"><span class="label">题目：</span>{{ r.problemName }}</div>
        <div class="row"><span class="label">语言：</span>{{ r.language }}</div>
        <div class="row"><span class="label">结果：</span>{{ r.result }}</div>
        <div class="row"><span class="label">耗时：</span>{{ r.timeUsed }} ms</div>
        <div class="row"><span class="label">内存：</span>{{ r.memoryUsed }} KB</div>
        <div class="row"><span class="label">提交时间：</span>{{ formatTime(r.submitTime) }}</div>

        <!-- 代码区域：默认折叠 -->
        <div class="row code-area">
          <span class="label">代码：</span>
          <button class="toggle-btn" @click="toggleCode(r)">
            {{ r._showCode ? '收起' : '展开' }}
          </button>
          <pre v-if="r._showCode" class="code-block">{{ r.code }}</pre>
        </div>
      </li>
    </ul>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, getCurrentInstance } from 'vue'

const instance = getCurrentInstance()

const REMOTE_SERVER_URL = import.meta.env.VITE_REMOTE_SERVER_URL

/* --------------- 数据 --------------- */
const records = ref([])
const role = ref('')
const name = ref('') // 当前登录用户名

/* --------------- 计算属性 --------------- */
const sortedRecords = computed(() => [...records.value].sort((a, b) => b.submitTime - a.submitTime))
const isPrivileged = computed(() => ['ROOT', 'MANAGER'].includes(role.value))

/* --------------- 工具 --------------- */
const parseJwt = (token) => {
  const base64Url = token.split('.')[1]
  const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/')
  const jsonPayload = decodeURIComponent(
    atob(base64)
      .split('')
      .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
      .join(''),
  )
  return JSON.parse(jsonPayload)
}
const formatTime = (ts) => new Date(ts).toLocaleString()

/* 代码折叠辅助 */
const toggleCode = (r) => {
  r._showCode = !r._showCode
}

/* --------------- 业务 --------------- */
const parseRole = () => {
  const token = localStorage.getItem('jwt')
  if (!token) return
  try {
    const payload = parseJwt(token)
    role.value = payload.role || ''
    name.value = payload.sub || ''
  } catch {
    alert('解析角色失败')
  }
}

const fetchRecords = async (userName = '') => {
  const token = localStorage.getItem('jwt')
  const params = new URLSearchParams(userName ? { pname: userName } : {})
  try {
    const res = await fetch(`${REMOTE_SERVER_URL}/practice/recordList/any?${params}`, {
      method: 'GET',
      headers: { Authorization: `Bearer ${token}` },
    })
    const json = await res.json()
    if (json.status === 0) {
      // 给每条记录加一个 _showCode 用于折叠
      records.value = (json.data ?? []).map((r) => ({ ...r, _showCode: false }))
    } else {
      alert(json.error || '拉取记录失败')
    }
  } catch (e) {
    alert('网络错误：' + e.message)
  }
}

/* --------------- 生命周期 --------------- */
onMounted(() => {
  parseRole()
  fetchRecords(name.value)
})

/* 按钮回调（由你补充） */
const searchByUser = () => {
  const u = prompt('请输入用户名：', name.value)
  if (u !== null) fetchRecords(u)
}

const searchAll = async () => {
  const token = localStorage.getItem('jwt')
  try {
    const res = await fetch(`${REMOTE_SERVER_URL}/practice/recordList/all?`, {
      method: 'GET',
      headers: { Authorization: `Bearer ${token}` },
    })
    const json = await res.json()
    if (json.status === 0) {
      // 给每条记录加一个 _showCode 用于折叠
      records.value = (json.data ?? []).map((r) => ({ ...r, _showCode: false }))
    } else {
      alert(json.error || '拉取记录失败')
    }
  } catch (e) {
    b
    alert('网络错误：' + e.message)
  }
}
</script>

<style scoped>
.record-list {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;
}
.action-bar {
  margin-bottom: 20px;
}
.action-bar button {
  margin-right: 12px;
}
.records {
  list-style: none;
  padding: 0;
}
.record {
  border: 1px solid #ddd;
  border-radius: 6px;
  padding: 12px 16px;
  margin-bottom: 12px;
}
.row {
  line-height: 1.6;
}
.label {
  font-weight: 600;
  margin-right: 6px;
}
.code-area {
  display: flex;
  align-items: flex-start;
}
.toggle-btn {
  margin-left: 8px;
  font-size: 0.85em;
  cursor: pointer;
}
.code-block {
  margin: 6px 0 0 0;
  padding: 8px;
  background: #f6f8fa;
  border: 1px solid #eaeaea;
  border-radius: 4px;
  overflow-x: auto;
  white-space: pre-wrap;
  word-break: break-all;
}
</style>
