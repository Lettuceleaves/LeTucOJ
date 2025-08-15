<template>
  <div class="contest-list">
    <div class="header">
      <h2>竞赛考核</h2>
      <div class="actions">
        <button v-if="isAdmin" @click="createContest">新建竞赛</button>
      </div>
    </div>

    <div v-if="loading" class="status">正在加载竞赛列表...</div>
    <div v-else-if="error" class="status error">加载失败：{{ error }}</div>
    <ul v-else class="contest-items">
      <li v-if="contests.length === 0 && !loading" class="empty">暂无竞赛</li>
      <li v-for="c in contests" :key="c.name" class="contest-item">
        <div class="info" @click="goDetail(c)">
          <div class="name">{{ c.cnname }}({{ c.name }})</div>
          <div class="meta">
            <span>{{ formatTime(c.start) }} - {{ formatTime(c.end) }}</span>
            <span v-if="inAssessment(c)" class="badge ongoing">进行中</span>
            <span v-else-if="beforeStart(c)" class="badge upcoming">未开始</span>
            <span v-else class="badge ended">已结束</span>
          </div>
          <div v-if="beforeStart(c)" class="countdown">倒计时：{{ countdowns[c.name] }}</div>
        </div>
        <div class="ops">
          <button v-if="isAdmin" @click.stop="editContest(c)">修改</button>
        </div>
      </li>
    </ul>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'

// const ip = import.meta.env.VITE_API_IP
const REMOTE_SERVER_URL = import.meta.env.VITE_REMOTE_SERVER_URL

const router = useRouter()

/* 数据 */
const contests = ref([])
const loading = ref(false)
const error = ref(null)
const countdowns = ref({})
const timerHandles = ref({})

/* 权限 */
const userRole = ref(null)
const isAdmin = computed(() => ['MANAGER', 'ROOT'].includes(userRole.value))

/* 工具 */
function parseToken() {
  const token = localStorage.getItem('jwt') || ''
  try {
    const [, payload64] = token.split('.')
    const payload = JSON.parse(
      decodeURIComponent(
        atob(payload64.replace(/-/g, '+').replace(/_/g, '/'))
          .split('')
          .map((c) => `%${c.charCodeAt(0).toString(16).padStart(2, '0')}`)
          .join(''),
      ),
    )
    userRole.value = payload.role
  } catch {
    /* ignore */
  }
}

function formatTime(ts) {
  const d = new Date(ts)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(
    d.getDate(),
  ).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(
    2,
    '0',
  )}`
}

function beforeStart(c) {
  return new Date(c.start).getTime() > Date.now()
}
function inAssessment(c) {
  const now = Date.now()
  const s = new Date(c.start).getTime()
  const e = new Date(c.end).getTime()
  return now >= s && now <= e
}
function getRemaining(c) {
  const delta = Math.max(0, new Date(c.start).getTime() - Date.now())
  const sec = Math.floor(delta / 1000)
  const m = Math.floor(sec / 60)
  const s = sec % 60
  return `${m}m${s}s`
}
function startCountdown(c) {
  countdowns.value[c.name] = getRemaining(c)
  timerHandles.value[c.name] = setInterval(() => {
    countdowns.value[c.name] = getRemaining(c)
    if (!beforeStart(c)) clearInterval(timerHandles.value[c.name])
  }, 1000)
}

/* 网络 */
async function fetchList() {
  loading.value = true
  error.value = null
  try {
    const token = localStorage.getItem('jwt') || ''
    const res = await fetch(`${REMOTE_SERVER_URL}/contest/list/contest`, {
      method: 'GET',
      headers: { Authorization: `Bearer ${token}`, Accept: 'application/json' },
    })
    const text = await res.text()
    if (text.trim().startsWith('<')) {
      error.value = '接口返回 HTML，路径/权限/后端错误，详见控制台'
      return
    }
    const json = JSON.parse(text)
    if (json.status === 0 || json.status === 1) {
      contests.value = json.data || []
      contests.value.forEach((c) => beforeStart(c) && startCountdown(c))
    } else {
      error.value = json.error || '未知错误'
    }
  } catch (e) {
    error.value = e.message || '请求失败'
  } finally {
    loading.value = false
  }
}

/* 交互 */
function goDetail(c) {
  router.push({ path: '/contest/detail', query: { ctname: c.name } })
}
function editContest(c) {
  router.push({ path: '/contest/form', query: { ctname: c.name } })
}
function createContest() {
  router.push({ path: '/contest/form' })
}

/* 生命周期 */
onMounted(() => {
  parseToken()
  fetchList()
})
onUnmounted(() => {
  Object.values(timerHandles.value).forEach(clearInterval)
})
</script>

<style scoped>
.contest-list {
  padding: 16px;
  max-width: 1000px;
  margin: 0 auto;
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.contest-items {
  list-style: none;
  padding: 0;
  margin: 0;
}
.contest-item {
  display: flex;
  justify-content: space-between;
  padding: 12px;
  border-bottom: 1px solid #e5e7eb;
  align-items: flex-start;
  gap: 12px;
}
.info {
  flex: 1;
  cursor: pointer;
}
.name {
  font-weight: bold;
  font-size: 1.1em;
}
.meta {
  margin-top: 4px;
  font-size: 0.9em;
  color: #555;
  display: flex;
  gap: 8px;
}
.badge {
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 0.75em;
  margin-left: 4px;
}
.ongoing {
  background: #10b981;
  color: white;
}
.upcoming {
  background: #f59e0b;
  color: white;
}
.ended {
  background: #9ca3af;
  color: white;
}
.countdown {
  margin-top: 6px;
  font-size: 0.9em;
  color: #1f2937;
}
.ops button {
  background: #3b82f6;
  border: none;
  color: white;
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
}
.empty {
  padding: 20px;
  color: #777;
}
.status {
  padding: 12px;
}
.status.error {
  color: #b91c1c;
}
</style>
