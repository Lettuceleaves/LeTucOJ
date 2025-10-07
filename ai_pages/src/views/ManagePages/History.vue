<template>
  <div class="record-list">
    <div v-if="isPrivileged" class="action-bar">
      <button @click="searchByUser">查找特定用户记录</button>
    </div>

    <div class="action-bar common-actions">
      <button class="home-btn red" @click="goHome">返回主页</button>
    </div>

    <div class="pagination-bar">
      <button :disabled="start === 0" @click="prevPage">上一页</button>
      <div class="page-info">第 {{ start / limit + 1 }} 页 / 共 {{ Math.ceil(total / limit) }} 页 (总数: {{ total }})</div>
      <button :disabled="start + limit >= total" @click="nextPage">下一页</button>
    </div>

    <ul class="records">
      <li v-for="r in sortedRecords" :key="r.submitTime" class="record">
        <div class="row"><span class="label">用户：</span>{{ r.userName }}({{ r.cnname }})</div>
        <div class="row"><span class="label">题目：</span>{{ r.problemName }}</div>
        <div class="row"><span class="label">语言：</span>{{ r.language }}</div>
        <div class="row"><span class="label">结果：</span>{{ r.result }}</div>
        <div class="row"><span class="label">耗时：</span>{{ r.timeUsed }} ms</div>
        <div class="row"><span class="label">内存：</span>{{ r.memoryUsed }} KB</div>
        <div class="row"><span class="label">提交时间：</span>{{ formatTime(r.submitTime) }}</div>

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
import { useRouter } from 'vue-router'

const instance = getCurrentInstance()
const ip = instance.appContext.config.globalProperties.$ip

/* 路由跳转 */
const router = useRouter()
const goHome = () => router.push('/main')

/* --------------- 数据 --------------- */
const records = ref([])
const role = ref('')
const name = ref('') // 当前登录用户名，虽然不用于默认查询，但可能用于 prompt 默认值

const start = ref(0)
const limit = 20
const total = ref(0)

// 默认查询模式改为 'all'
const currentSearchMode = ref('all') // 'any' or 'all'
const currentSearchUser = ref('')    // The user being queried

/* --------------- 计算属性 --------------- */
const sortedRecords = computed(() =>
  [...records.value].sort((a, b) => b.submitTime - a.submitTime)
)
const isPrivileged = computed(() => ['ROOT', 'MANAGER'].includes(role.value))

/* --------------- 工具 --------------- */
const parseJwt = (token) => {
  const base64Url = token.split('.')[1]
  const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/')
  const jsonPayload = decodeURIComponent(
    atob(base64)
      .split('')
      .map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
      .join('')
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
    name.value = payload.sub || '' // 仍解析用户名，用于搜索时的默认值
  } catch {
    alert('解析角色失败')
  }
}

// 通用的数据获取函数，根据参数决定查询所有或特定用户
const fetchData = async (userName = '', isAll = false) => {
  const token = localStorage.getItem('jwt')
  const params = new URLSearchParams({
    start: start.value,
    limit: limit
  })

  // 区分 API 路径
  const apiUrl = isAll ? `/practice/recordList/all` : `/practice/recordList/any`

  // 只有在非查询全部模式且提供了用户名时，才添加 pname 参数
  if (userName && !isAll) {
    params.append('pname', userName)
  }

  try {
    const res = await fetch(`http://${ip}${apiUrl}?${params}`, {
      method: 'GET',
      headers: { Authorization: `Bearer ${token}` }
    })
    const json = await res.json()
    if (json.code === "0") {
      records.value = (json.data.records ?? []).map(r => ({ ...r, _showCode: false }))
      total.value = json.data?.amount || 0
    } else {
      alert(json.error || '拉取记录失败')
    }
  } catch (e) {
    alert('网络错误：' + e.message)
  }
}

/* --------------- 按钮回调 --------------- */
const searchByUser = () => {
  // 弹出提示框，使用当前登录用户名作为默认值
  const u = prompt('请输入要查找的用户名：', name.value || '')
  if (u !== null && u.trim() !== '') {
    // 设置模式和用户名，重置页码
    currentSearchMode.value = 'any' // 更改为 'any' 表示搜索特定用户
    currentSearchUser.value = u.trim()
    start.value = 0
    // 调用通用函数，查询特定用户
    fetchData(currentSearchUser.value, false)
  } else if (u !== null && u.trim() === '') {
      alert('用户名不能为空')
  }
}

const searchAll = () => {
  // 设置模式，重置页码
  currentSearchMode.value = 'all'
  currentSearchUser.value = ''
  start.value = 0
  // 调用通用函数，查询所有用户
  fetchData('', true)
}

const prevPage = () => {
  if (start.value > 0) {
    start.value -= limit
    // 根据当前模式调用通用函数
    fetchData(currentSearchUser.value, currentSearchMode.value === 'all')
  }
}

const nextPage = () => {
  if (start.value + limit < total.value) {
    start.value += limit
    // 根据当前模式调用通用函数
    fetchData(currentSearchUser.value, currentSearchMode.value === 'all')
  }
}

/* --------------- 生命周期 --------------- */
onMounted(() => {
  parseRole()
  // 页面加载时默认查询所有用户记录 (符合管理员页面的新逻辑)
  currentSearchMode.value = 'all'
  currentSearchUser.value = ''
  fetchData('', true)
})
</script>

<style scoped>
/* 样式代码保持不变，因为它已经符合要求 */
.record-list {
  max-width: 960px;
  margin: 0 auto;
  padding: 24px;
  font-family: "Segoe UI", Roboto, sans-serif;
  color: #333;
}

/* 顶部操作栏 */
.action-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}
.action-bar button {
  background: #2563eb;
  color: #fff;
  padding: 8px 14px;
  border-radius: 6px;
  border: none;
  cursor: pointer;
  font-size: 0.95em;
  transition: background 0.2s;
}
.action-bar button:hover {
  background: #2563eb;
}

/* 列表 */
.records {
  list-style: none;
  padding: 0;
  margin: 0;
}
.record {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  padding: 16px 20px;
  margin-bottom: 16px;
  box-shadow: 0 2px 6px rgba(0,0,0,0.06);
  transition: transform 0.2s, box-shadow 0.2s;
}
.record:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
}

/* 行 */
.row {
  line-height: 1.7;
  margin-bottom: 6px;
}
.label {
  font-weight: 600;
  margin-right: 6px;
  color: #555;
}
.row:last-child {
  margin-bottom: 0;
}

/* 提交时间 */
.row span.label + span,
.row span.label + div {
  color: #666;
  font-size: 0.92em;
}

/* 代码区域 */
.code-area {
  margin-top: 10px;
  display: flex;
  align-items: flex-start;
}
.toggle-btn {
  margin-left: 8px;
  font-size: 0.85em;
  cursor: pointer;
  background: #10b981;
  color: #fff;
  border: none;
  padding: 4px 10px;
  border-radius: 4px;
  transition: background 0.2s;
}
.toggle-btn:hover {
  background: #059669;
}
.code-block {
  margin: 10px 0 0 0;
  padding: 10px;
  background: #1e293b;
  color: #f8fafc;
  border-radius: 6px;
  overflow-x: auto;
  white-space: pre;
  font-family: "Fira Code", "Courier New", monospace;
  font-size: 0.9em;
  line-height: 1.5;
}
/* 红色皮肤 */
button.red {
  background: #ef4444; /* Tailwind red-500 */
}
button.red:hover {
  background: #dc2626; /* Tailwind red-600 */
}

/* Pagination bar styling */
.pagination-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 10px 0;
}
.pagination-bar button {
  background: #2563eb;
  color: #fff;
  padding: 8px 14px;
  border-radius: 6px;
  border: none;
  cursor: pointer;
  font-size: 0.95em;
  transition: background 0.2s;
}
.pagination-bar button:hover:not([disabled]) {
  background: #1d4ed8;
}
.pagination-bar button:disabled {
  background: #d1d5db;
  cursor: not-allowed;
}
.page-info {
  font-size: 1em;
  color: #6b7280;
  white-space: nowrap;
}
</style>