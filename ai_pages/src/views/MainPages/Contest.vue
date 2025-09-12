<template>
  <div class="contest-list">
    <!-- 标题栏 -->
    <div class="header">
      <h2>🏆 竞赛考核</h2>
      <div class="actions">
        <button v-if="isAdmin" class="btn primary" @click="createContest">+ 新建竞赛</button>
      </div>
    </div>

    <!-- 状态显示 -->
    <div v-if="loading" class="status">正在加载竞赛列表...</div>
    <div v-else-if="error" class="status error">加载失败：{{ error }}</div>

    <!-- 列表 -->
    <ul v-else class="contest-items">
      <li v-if="contests.length === 0" class="empty">暂无竞赛</li>

      <li
        v-for="c in contests"
        :key="c.name"
        class="contest-item"
      >
        <!-- 左侧信息块 -->
        <div class="info" @click="goDetail(c)">
          <div class="name">{{ c.cnname }} <span class="en-name">({{ c.name }})</span></div>
          <div class="meta">
            <span class="time">{{ formatTime(c.start) }} - {{ formatTime(c.end) }}</span>
            <span
              class="badge"
              :class="{
                ongoing: inAssessment(c),
                upcoming: beforeStart(c),
                ended: !beforeStart(c) && !inAssessment(c)
              }"
            >
              {{ inAssessment(c) ? '进行中' : beforeStart(c) ? '未开始' : '已结束' }}
            </span>
          </div>
          <div v-if="beforeStart(c)" class="countdown">
            ⏳ 倒计时：<span>{{ countdowns[c.name] }}</span>
          </div>
        </div>

        <!-- 右侧操作按钮 -->
        <div class="ops">
          <button v-if="isAdmin" class="btn secondary" @click.stop="editContest(c)">修改</button>
        </div>
      </li>
    </ul>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, getCurrentInstance } from 'vue'
import { useRouter } from 'vue-router'

const instance = getCurrentInstance()
const ip = instance.appContext.config.globalProperties.$ip
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
          .map(c => `%${c.charCodeAt(0).toString(16).padStart(2, '0')}`)
          .join('')
      )
    )
    userRole.value = payload.role
  } catch {
    /* ignore */
  }
}

function formatTime(ts) {
  const d = new Date(ts)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(
    d.getDate()
  ).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(
    d.getMinutes()
  ).padStart(2, '0')}`
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
  return `${m}分${s}秒`
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
    const res = await fetch(`http://${ip}/contest/list/contest`, {
      method: 'GET',
      headers: { Authorization: `Bearer ${token}`, Accept: 'application/json' }
    })
    const text = await res.text()
    if (text.trim().startsWith('<')) {
      error.value = '接口返回 HTML，可能是路径或权限问题'
      return
    }
    const json = JSON.parse(text)
    if (json.status === 0 || json.status === 1) {
      contests.value = json.data || []
      contests.value.forEach(c => beforeStart(c) && startCountdown(c))
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
  padding: 20px;
  max-width: 1000px;
  margin: 0 auto;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  font-family: 'Helvetica Neue', Arial, sans-serif;
}

/* 标题栏 */
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.header h2 {
  font-size: 1.5rem;
  font-weight: 700;
  color: #111827;
}

/* 操作按钮 */
.btn {
  padding: 8px 16px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: 0.3s;
}
.btn.primary {
  background: #2563eb;
  color: #fff;
}
.btn.primary:hover {
  background: #1e4db7;
}
.btn.secondary {
  background: #10b981;
  color: #fff;
}
.btn.secondary:hover {
  background: #059669;
}

/* 列表 */
.contest-items {
  list-style: none;
  padding: 0;
  margin: 0;
}
.contest-item {
  background: #f9fafb;
  margin-bottom: 12px;
  padding: 16px;
  border-radius: 8px;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  transition: box-shadow 0.3s, transform 0.3s;
}
.contest-item:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  transform: translateY(-2px);
}

/* 信息部分 */
.info {
  flex: 1;
  cursor: pointer;
}
.name {
  font-size: 18px;
  font-weight: 600;
  color: #111827;
}
.en-name {
  font-size: 14px;
  color: #6b7280;
}
.meta {
  margin-top: 6px;
  font-size: 14px;
  color: #4b5563;
  display: flex;
  align-items: center;
  gap: 10px;
}
.time {
  font-weight: 500;
}

/* 状态标签 */
.badge {
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 12px;
  color: white;
}
.ongoing {
  background: #10b981;
}
.upcoming {
  background: #f59e0b;
}
.ended {
  background: #9ca3af;
}

/* 倒计时 */
.countdown {
  margin-top: 8px;
  font-size: 14px;
  color: #1f2937;
  font-weight: 500;
}

/* 操作按钮右侧 */
.ops button {
  font-size: 13px;
}

/* 空状态与加载 */
.empty {
  padding: 20px;
  text-align: center;
  color: #6b7280;
}
.status {
  padding: 12px;
  text-align: center;
}
.status.error {
  color: #b91c1c;
  font-weight: bold;
}
</style>
