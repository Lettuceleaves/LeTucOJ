<template>
  <div class="record-list">
    <!-- 仅 ROOT / MANAGER 可见 -->
    <div v-if="isPrivileged" class="action-bar">
      <button @click="searchByUser">查找特定用户记录</button>
      <button @click="searchAll">查找全部用户记录</button>
    </div>
    
    <div class="action-bar common-actions">
      <button class="home-btn red" @click="goHome">返回主页</button>
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
import { useRouter } from 'vue-router'

const instance = getCurrentInstance()
const ip = instance.appContext.config.globalProperties.$ip

/* 路由跳转 */
const router = useRouter()

const goHome = () => router.push('/main')   // 根据你的路由表调整路径

/* --------------- 数据 --------------- */
const records = ref([])
const role   = ref('')
const name   = ref('')        // 当前登录用户名

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
    name.value = payload.sub  || ''
  } catch {
    alert('解析角色失败')
  }
}

const fetchRecords = async (userName = '') => {
  const token = localStorage.getItem('jwt')
  const params = new URLSearchParams(userName ? { pname: userName } : {})
  try {
    const res = await fetch(`http://${ip}/practice/recordList/any?${params}`, {
      method: 'GET',
      headers: { Authorization: `Bearer ${token}` }
    })
    const json = await res.json()
    if (json.status === 0) {
      // 给每条记录加一个 _showCode 用于折叠
      records.value = (json.data ?? []).map(r => ({ ...r, _showCode: false }))
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
    const res = await fetch(`http://${ip}/practice/recordList/all?`, {
      method: 'GET',
      headers: { Authorization: `Bearer ${token}` }
    })
    const json = await res.json()
    if (json.status === 0) {
      // 给每条记录加一个 _showCode 用于折叠
      records.value = (json.data ?? []).map(r => ({ ...r, _showCode: false }))
    } else {
      alert(json.error || '拉取记录失败')
    }
  } catch (e) {b
    alert('网络错误：' + e.message)
  }
}
</script>

<style scoped>
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
  background: #ef4444;        /* Tailwind red-500 */
}
button.red:hover {
  background: #dc2626;        /* Tailwind red-600 */
}

</style>

