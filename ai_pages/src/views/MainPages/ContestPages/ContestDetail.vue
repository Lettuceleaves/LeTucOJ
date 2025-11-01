<template>
  <div class="contest-detail">
    <div class="header">
      <div class="left">
        <h2>{{ effectiveContest.cnname || effectiveContest.name }}</h2>
        <div class="time-range">
          {{ formatTime(effectiveContest.start) }} - {{ formatTime(effectiveContest.end) }}
          <span v-if="beforeStart" class="status upcoming">
            未开始（倒计时 {{ countdown }}）
          </span>
          <span v-else-if="inAssessment" class="status ongoing">进行中</span>
          <span v-else class="status ended">已结束</span>
        </div>
      </div>
      <div class="actions">
        <button class="btn-return" @click="handleReturn">返回</button>
        <button v-if="!attended" @click="handleAttend" :disabled="attending">
          {{ attending ? '参加中...' : '参加竞赛' }}
        </button>
        <button v-else disabled>已参加</button>
      </div>
    </div>

    <div v-if="loading" class="status">加载中...</div>
    <div v-else-if="error" class="status error">错误：{{ error }}</div>
    <div v-else class="body">
      <section class="info">
        <p><strong>模式：</strong>{{ effectiveContest.mode }}</p>

        <div class="info-item">
          <strong>备注：</strong>
          <div v-if="effectiveContest.note" class="note-content">
            <MarkdownRenderer :rawContent="effectiveContest.note" />
          </div>
          <span v-else>无</span>
        </div>

        <p><strong>公开：</strong>{{ effectiveContest.publicContest ? '是' : '否' }}</p>
      </section>

      <section class="problems">
        <div class="section-header">
          <h3>题目列表</h3>
          <div class="subnote" v-if="!inAssessment">
            目前不是考核时间，题目不可作答。{{ remainingText }}
          </div>
        </div>

        <div v-if="problemsLoading" class="status">正在加载题目...</div>
        <div v-else-if="problemsError && !problemsError.includes('No problems found')" class="status error">
          加载题目失败：{{ problemsError }}
          <button @click="fetchProblemList" class="retry-btn">重试</button>
        </div>

        <template v-else>
          <ul class="problem-list">
            <li
              v-for="p in problemList"
              :key="p.problemName"
              class="problem-item"
              @click="goToProblem(p)"
            >
              <div class="left">
                <div class="title">{{ p.problemName }}</div>
                <div class="meta">分数：{{ p.score }}</div>
              </div>
              <div v-if="isAdmin" class="right">
                <button
                  class="btn-delete"
                  @click.stop="openDeleteConfirm(p.problemName)"
                >
                  删除
                </button>
              </div>
            </li>

            <li v-if="problemList.length === 0 && !isAddingProblem" class="empty">
              当前没有题目
            </li>

            <li v-if="isAddingProblem" class="problem-item add-problem-row">
              <div class="add-form">
                <input v-model="newProblemName" placeholder="请输入题目名称" />
                <input
                  v-model.number="newProblemScore"
                  type="number"
                  placeholder="分数"
                  style="width: 100px"
                />
                <div class="controls">
                  <button class="btn-ok" @click="confirmAddProblem">确定</button>
                  <button class="btn-cancel" @click="cancelAddProblem">取消</button>
                </div>
              </div>
            </li>

            <li
              v-if="isAdmin && !isAddingProblem"
              class="problem-item add-button-row"
              @click="startAddProblem"
            >
              <button class="add-problem-btn">+ 添加题目</button>
            </li>
          </ul>
        </template>
      </section>

      <section class="leaderboard">
        <div class="section-header"><h3>榜单</h3></div>

        <div v-if="leaderboardData.length === 0" class="placeholder">
          <p>暂无提交记录</p>
        </div>

        <div v-else class="board">
          <div class="board-header" :style="{ '--cols': colCount }">
            <div class="cell th left-sticky">排名 / 用户</div>
            <div class="cell th" v-for="p in uniqueProblems" :key="p">{{ p }}</div>
            <div class="cell th">总分</div>
            <div class="cell th">最后提交</div>
          </div>

          <div class="board-body" :style="{ '--cols': colCount }">
            <div
              class="board-row"
              v-for="(row, idx) in rows"
              :key="row.user"
            >
              <div class="cell left-sticky user-cell">
                <span class="rank" :class="medalClass(idx)">{{ idx + 1 }}</span>
                <span class="avatar">{{ (row.userCnname || row.user).slice(0,1).toUpperCase() }}</span>
                <span class="uname" :title="row.user">{{ row.userCnname || row.user }}</span>
              </div>

              <div
                class="cell score"
                v-for="p in uniqueProblems"
                :key="p"
                :style="scoreBgStyle(row.scores[p] ?? 0, p)"
                :class="{ pass: (row.scores[p] ?? 0) > 0 }"
              >
                <span class="score-text">{{ row.scores[p] ?? 0 }}</span>
              </div>

              <div class="cell total">
                <div class="bar">
                  <div class="bar-inner" :style="{ width: totalPercent(row) }"></div>
                </div>
                <span class="total-text">{{ row.totalScore }}</span>
              </div>

              <div class="cell">{{ formatTime(row.lastSubmit) }}</div>
            </div>
          </div>
        </div>
      </section>

    </div>
  </div>

  <div v-if="deleteConfirmVisible" class="modal-overlay" @click.self="closeDeleteConfirm">
    <div class="modal">
      <p>确定删除题目 “{{ deleteTargetName }}” 吗？</p>
      <div class="modal-actions">
        <button @click="confirmDeleteProblem">确定</button>
        <button @click="closeDeleteConfirm">取消</button>
      </div>
    </div>
  </div>

</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, getCurrentInstance } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import MarkdownRenderer from '../../../components/MarkdownRenderer.vue'

const instance = getCurrentInstance()
const ip = instance.appContext.config.globalProperties.$ip
const route = useRoute()
const router = useRouter()
const ctname = route.query.ctname

// Props
const props = defineProps({
  contest: {
    type: Object,
    required: true
  }
})

// Emits
const emit = defineEmits(['close', 'view-leaderboard'])

// --- State ---
const fullContest = ref({})
const loading = ref(false)
const error = ref(null)

const problemList = ref([])
const problemsLoading = ref(false)
const problemsError = ref(null)

const attended = ref(false)
const attending = ref(false)
const countdown = ref('')
let timer = null

const isAddingProblem = ref(false)
const newProblemName = ref('')
const newProblemScore = ref(100)

const deleteConfirmVisible = ref(false)
const deleteTargetName = ref('')

const leaderboardData = ref([])

// --- 用户权限管理 ---
const userRole = ref('') // 统一存储用户角色

// [OPTIMIZED] 使用计算属性统一判断管理员权限
const isAdmin = computed(() => ['ROOT', 'MANAGER'].includes(userRole.value))

// [OPTIMIZED] 统一的JWT解析和角色设置函数
function initializeUserRole() {
  const token = localStorage.getItem('jwt')
  if (!token) return
  try {
    const base64Url = token.split('.')[1]
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/')
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split('')
        .map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    )
    const payload = JSON.parse(jsonPayload)
    userRole.value = payload.role || ''
  } catch (e) {
    console.error('解析 JWT 失败', e)
    userRole.value = '' // 解析失败则清空角色
  }
}

// --- Computed Properties ---
const effectiveContest = computed(() =>
  Object.keys(fullContest.value).length ? fullContest.value : props.contest || {}
)

const inAssessment = computed(() => {
  const c = effectiveContest.value
  if (!c.start || !c.end) return false
  const now = Date.now()
  const s = new Date(c.start).getTime()
  const e = new Date(c.end).getTime()
  return now >= s && now <= e
})

const beforeStart = computed(() => {
  const c = effectiveContest.value
  if (!c.start) return false
  return Date.now() < new Date(c.start).getTime()
})

const remainingText = computed(() => {
  if (!beforeStart.value || !effectiveContest.value.start) return ''
  const delta = new Date(effectiveContest.value.start).getTime() - Date.now()
  if (delta <= 0) return ''
  const sec = Math.floor(delta / 1000)
  const m = Math.floor(sec / 60)
  const s = sec % 60
  return `还有 ${m}m${s}s 开始`
})

// --- Leaderboard Computeds ---
const uniqueProblems = computed(() => {
  const problemNames = new Set()
  leaderboardData.value.forEach(item => {
    if (item.problemName) {
      problemNames.add(item.problemName)
    }
  })
  return Array.from(problemNames)
})

const colCount = computed(() => uniqueProblems.value.length + 3)

const groupedByUser = computed(() => {
  const map = {}
  for (const r of leaderboardData.value) {
    if (!map[r.userName]) {
      map[r.userName] = {
        user: r.userName,
        userCnname: r.userCnname,
        scores: {},
        lastSubmit: '1970-01-01T00:00:00Z',
        totalScore: 0
      }
    }
    map[r.userName].scores[r.problemName] = r.score
  }
  
  // Recalculate totals and last submit time
  for (const user in map) {
    map[user].totalScore = Object.values(map[user].scores).reduce((a, b) => a + b, 0);
    const userSubmissions = leaderboardData.value.filter(sub => sub.userName === user);
    if (userSubmissions.length > 0) {
      map[user].lastSubmit = userSubmissions.reduce((latest, current) => 
        new Date(current.lastSubmit) > new Date(latest.lastSubmit) ? current : latest
      ).lastSubmit;
    }
  }

  return map
})

const rows = computed(() => {
  const arr = Object.values(groupedByUser.value)
  arr.sort((a, b) => {
    if (b.totalScore !== a.totalScore) {
      return b.totalScore - a.totalScore
    }
    return new Date(a.lastSubmit) - new Date(b.lastSubmit)
  })
  return arr
})

const maxTotal = computed(() => {
  const totalScores = problemList.value.reduce((sum, p) => sum + (Number(p.score) || 0), 0)
  return totalScores > 0 ? totalScores : 100; // Fallback to 100 if no problems/scores
})

const problemMaxMap = computed(() => {
  return problemList.value.reduce((map, p) => {
    map[p.problemName] = Number(p.score) || 0
    return map
  }, {})
})

// --- Methods ---
function formatTime(ts) {
  if (!ts) return ''
  const d = new Date(ts)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(
    d.getDate()
  ).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(
    d.getMinutes()
  ).padStart(2, '0')}`
}

// Data Fetching
async function fetchFullContest() {
  loading.value = true
  error.value = null
  try {
    const token = localStorage.getItem('jwt') || ''
    const res = await fetch(`http://${ip}/contest/full/getContest?ctname=${ctname}`, {
      headers: { Authorization: `Bearer ${token}` }
    })
    const json = await res.json()
    if (json.code === '0' && json.data) {
      fullContest.value = json.data
    } else {
      error.value = json.message || '获取竞赛详情失败'
    }
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

async function fetchProblemList() {
  problemsLoading.value = true
  problemsError.value = null
  try {
    const token = localStorage.getItem('jwt') || ''
    const res = await fetch(`http://${ip}/contest/list/problem?ctname=${ctname}`, {
      headers: { Authorization: `Bearer ${token}` }
    })
    const json = await res.json()
    if (json.code === '0' && Array.isArray(json.data)) {
      problemList.value = json.data
    } else if (json.code === 'B020007') {
      problemList.value = [] // No problems found is not an error
    } else {
      problemsError.value = json.message || '获取题目列表失败'
    }
  } catch (e) {
    problemsError.value = e.message
  } finally {
    problemsLoading.value = false
  }
}

async function fetchLeaderboard() {
  try {
    const token = localStorage.getItem('jwt')
    const res = await fetch(`http://${ip}/contest/list/board?ctname=${ctname}`, {
      headers: { Authorization: `Bearer ${token}` }
    })
    const json = await res.json()
    if (json.code === '0' && Array.isArray(json.data)) {
      leaderboardData.value = json.data
    } else if (json.code === 'B020009') {
      leaderboardData.value = []
    } else {
      console.warn('获取榜单失败：', json.message)
    }
  } catch (e) {
    console.error('获取榜单异常', e)
  }
}

async function fetchAttendStatus() {
  try {
    const token = localStorage.getItem('jwt')
    const res = await fetch(`http://${ip}/contest/inContest?ctname=${ctname}`, {
      headers: { Authorization: `Bearer ${token}` }
    })
    const json = await res.json()
    attended.value = json.code === '0'
  } catch (e) {
    console.error('获取参赛状态失败', e)
  }
}


// Problem Management
function startAddProblem() {
  isAddingProblem.value = true
  newProblemName.value = ''
  newProblemScore.value = 100
}

function cancelAddProblem() {
  isAddingProblem.value = false
}

async function confirmAddProblem() {
  const name = newProblemName.value.trim()
  if (!name) return alert('请输入题目名称')

  try {
    const token = localStorage.getItem('jwt')
    const res = await fetch(`http://${ip}/contest/insertProblem`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${token}`
      },
      body: JSON.stringify({
        contestName: ctname,
        problemName: name,
        score: newProblemScore.value
      })
    })
    const json = await res.json()
    if (json.code === '0') {
      await fetchProblemList()
      cancelAddProblem()
    } else if (json.code === 'B010002') {
      alert(json.message || '题目未在题库中，请先在题库创建后再添加')
    } else {
      alert(`添加失败：${json.message || '未知错误'}`)
    }
  } catch (e) {
    alert(`添加失败：${e.message}`)
  }
}

function openDeleteConfirm(name) {
  deleteTargetName.value = name
  deleteConfirmVisible.value = true
}

function closeDeleteConfirm() {
  deleteConfirmVisible.value = false
  deleteTargetName.value = ''
}

async function confirmDeleteProblem() {
  try {
    const token = localStorage.getItem('jwt')
    const res = await fetch(`http://${ip}/contest/deleteProblem`, {
      method: 'DELETE',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${token}`
      },
      body: JSON.stringify({
        contestName: ctname,
        problemName: deleteTargetName.value
      })
    })
    const json = await res.json()
    if (json.code === '0') {
      await fetchProblemList()
      closeDeleteConfirm()
    } else {
      alert(`删除失败：${json.message || '未知错误'}`)
    }
  } catch (e) {
    alert(`删除失败：${e.message}`)
  }
}

// UI and Navigation
function goToProblem(p) {
  router.push({
    path: '/contest/editor',
    query: { contest: effectiveContest.value.name, problem: p.problemName }
  })
}

async function handleAttend() {
  attending.value = true
  try {
    const token = localStorage.getItem('jwt') || ''
    const res = await fetch(`http://${ip}/contest/attend?ctname=${ctname}`, {
      method: 'POST',
      headers: { Authorization: `Bearer ${token}` }
    })
    const json = await res.json()
    if (json.code === '0') {
      attended.value = true
    } else {
      alert(`参加失败：${json.message || '未知错误'}`)
    }
  } catch (e) {
    alert(`参加出错：${e.message}`)
  } finally {
    attending.value = false
  }
}

// Countdown Timer
function startCountdownIfNeeded() {
  if (timer) clearInterval(timer)
  if (beforeStart.value) {
    updateCountdown()
    timer = setInterval(updateCountdown, 1000)
  }
}

function updateCountdown() {
  if (!effectiveContest.value.start) return
  const delta = new Date(effectiveContest.value.start).getTime() - Date.now()
  if (delta <= 0) {
    countdown.value = ''
    if (timer) clearInterval(timer)
    // You might want to refetch contest data here to update status
    return
  }
  const sec = Math.floor(delta / 1000)
  const m = Math.floor(sec / 60)
  const s = sec % 60
  countdown.value = `${m}m${s}s`
}

// Leaderboard Display Helpers
function totalPercent(row) {
  if (!maxTotal.value) return '0%'
  return `${Math.round((row.totalScore / maxTotal.value) * 100)}%`
}

function medalClass(index) {
  if (index === 0) return 'gold'
  if (index === 1) return 'silver'
  if (index === 2) return 'bronze'
  return ''
}

function maxScoreOf(problemName) {
  return problemMaxMap.value[problemName] ?? 0
}

function scoreBgStyle(score, problemName) {
  const max = maxScoreOf(problemName)
  const s = Number(score) || 0
  const base = { color: '#111827' }

  if (!max || s <= 0) return { ...base, background: '#ffffff' }
  if (s >= max) return { ...base, background: '#FFD700' }

  const pct = Math.max(0, Math.min(100, (s / max) * 100))
  return {
    ...base,
    background: `linear-gradient(90deg, rgba(40,167,69,.35) ${pct}%, transparent ${pct}%)`
  }
}

function handleReturn() {
  router.push('/main?tab=contest') 
}

// Lifecycle Hooks
onMounted(async () => {
  initializeUserRole() // 初始化用户角色
  await fetchFullContest()
  await Promise.all([
      fetchProblemList(),
      fetchAttendStatus(),
      fetchLeaderboard()
  ])
  startCountdownIfNeeded()
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
/* 外层 80% 宽，水平居中 */
.board-wrapper {
  width: 80%;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(var(--cols), 1fr);
  grid-auto-rows: 1fr;
}

.board-row { display: contents; }

.cell {
  border: 1px solid #e3e6f0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  word-break: break-word;
  background: #fff;
}

.header-row .cell {
  background: #f8f9fa;
  font-weight: 600;
}

/* ---------- 通用变量 ---------- */
:root {
  --primary: #28a745;        /* 主色改为绿色 */
  --primary-hover: #1e7e34;
  --danger: #e74c3c;
  --success: #007bff;        /* 互换：确定按钮用蓝 */
  --muted: #6c757d;
  --border: #e3e6f0;
  --bg-light: #f8f9fa;
  --radius: 4px;
  --shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
}

/* ---------- 页面整体 ---------- */
.contest-detail {
  max-width: 900px;
  margin: 0 auto;
  padding: 24px;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
  line-height: 1.5;
  color: #212529;
}

/* ---------- 头部区域 ---------- */
.header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  border-bottom: 1px solid var(--border);
  padding-bottom: 16px;
  margin-bottom: 24px;
}

.left h2 {
  margin: 0 0 8px;
  font-size: 28px;
  font-weight: 600;
}

.time-range {
  font-size: 14px;
  color: var(--muted);
}

.status {
  margin-left: 8px;
  padding: 2px 6px;
  border-radius: var(--radius);
  font-size: 12px;
  font-weight: 600;
}

.status.upcoming {
  background: #fff3cd;
  color: #856404;
}

.status.ongoing {
  background: #d1ecf1;
  color: #0c5460;
}

.status.ended {
  background: #f8d7da;
  color: #721c24;
}

.actions button {
  margin-left: 8px;
  padding: 6px 14px;
  font-size: 14px;
  border: 1px solid var(--border);
  border-radius: var(--radius);
  background: #fff;
  color: var(--muted);
  cursor: pointer;
  transition: all 0.2s;
}

.actions button:hover:not(:disabled) {
  border-color: var(--primary);
  color: var(--primary);
}

.actions button:disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

/* ---------- 内容区域 ---------- */
.body {
  display: flex;
  flex-direction: column;
  gap: 32px;
}

section.info p {
  margin: 4px 0;
  font-size: 14px;
}

/* ---------- 小标题 ---------- */
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.section-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.section-header .subnote {
  font-size: 12px;
  color: var(--muted);
}

.section-header button {
  font-size: 12px;
  padding: 4px 8px;
  border: 1px solid var(--border);
  border-radius: var(--radius);
  background: #fff;
  cursor: pointer;
  transition: all 0.2s;
}

.section-header button:hover {
  border-color: var(--primary);
  color: var(--primary);
}

/* ---------- 题目列表 ---------- */
.problem-list {
  list-style: none;
  margin: 0;
  padding: 0;
}

.problem-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border: 1px solid var(--border);
  border-radius: var(--radius);
  margin-bottom: 8px;
  background: #fff;
  box-shadow: var(--shadow);
  cursor: pointer;
  transition: background 0.2s, border-color 0.2s;
}

.problem-item:hover {
  background: var(--bg-light);
  border-color: var(--primary);
}

.problem-item.empty {
  text-align: center;
  color: var(--muted);
  cursor: default;
}

.problem-item .title {
  font-weight: 600;
  margin-bottom: 4px;
}

.problem-item .meta {
  font-size: 12px;
  color: var(--muted);
}

/* ---------- 添加题目交互 ---------- */
.add-problem-row {
  padding: 16px;
  background: #f8f9fa; /* 改为浅灰色背景 */
  border: 1px solid #dee2e6; /* 改为灰色边框 */
  border-radius: var(--radius);
  margin-bottom: 8px;
}

.add-form {
  display: flex;
  gap: 12px;
  align-items: center;
}

.add-form input {
  flex: 1;
  padding: 8px 12px;
  border: 1px solid #ced4da;
  border-radius: var(--radius);
  font-size: 14px;
  background-color: #fff;
}

.add-form input:focus {
  outline: none;
  border-color: #007bff;
  box-shadow: 0 0 0 3px rgba(0, 123, 255, 0.25);
}

.add-form .controls {
  display: flex;
  gap: 8px;
}

.add-form .controls button {
  padding: 8px 16px;
  font-size: 14px;
  border: none;
  border-radius: var(--radius);
  cursor: pointer;
  font-weight: 500;
  transition: all 0.2s ease;
}

/* 确认按钮样式 */
.add-form .controls .btn-ok {
  background: #28a745; /* 绿色确认按钮 */
  color: #ffffff;
  box-shadow: 0 2px 4px rgba(40, 167, 69, 0.2);
}

.add-form .controls .btn-ok:hover {
  background: #218838;
  box-shadow: 0 4px 8px rgba(40, 167, 69, 0.3);
}

/* 取消按钮样式 */
.add-form .controls .btn-cancel {
  background: #6c757d; /* 灰色取消按钮 */
  color: #ffffff;
  box-shadow: 0 2px 4px rgba(108, 117, 125, 0.2);
}

.add-form .controls .btn-cancel:hover {
  background: #5a6268;
  box-shadow: 0 4px 8px rgba(108, 117, 125, 0.3);
}

/* ---------- 添加按钮行 ---------- */
.add-button-row {
  text-align: center;
  margin-top: 8px;
}

.add-problem-btn {
  display: inline-block;
  padding: 6px 16px;
  border: 1px dashed var(--primary);
  color: var(--primary);
  background: transparent;
  border-radius: var(--radius);
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}

.add-problem-btn:hover {
  background: var(--primary);
  color: #fff;
}

/* ---------- 状态提示 ---------- */
.status {
  font-size: 14px;
  color: var(--muted);
}

.error {
  color: var(--danger);
}

.retry-btn {
  margin-left: 8px;
  padding: 2px 6px;
  font-size: 12px;
  border: 1px solid var(--border);
  border-radius: var(--radius);
  background: #fff;
  cursor: pointer;
  transition: all 0.2s;
}

.retry-btn:hover {
  border-color: var(--primary);
  color: var(--primary);
}

/* ---------- 榜单占位 ---------- */
.placeholder {
  text-align: center;
  color: var(--muted);
  padding: 24px;
  border: 1px dashed var(--border);
  border-radius: var(--radius);
}

/* 退出按钮 */
.actions button:first-child {
  background: #007bff;
  color: #fff;
  border: 1px solid #007bff;
  border-radius: 6px;
}
.actions button:first-child:hover:not(:disabled) {
  background: #0056b3;
  border-color: #0056b3;
}

/* 参加竞赛按钮 */
.actions button:nth-child(2):not(:disabled) {
  background: #007bff;
  color: #fff;
  border: 1px solid #007bff;
  border-radius: 6px;
}
.actions button:nth-child(2):not(:disabled):hover {
  background: #0056b3;
  border-color: #0056b3;
}

/* 重试按钮 */
.retry-btn {
  background: #007bff;
  color: #fff;
  border: 1px solid #007bff;
  border-radius: 4px;
}
.retry-btn:hover {
  background: #0056b3;
  border-color: #0056b3;
}

/* 统一圆角蓝色按钮 */
.btn-ok,
.btn-cancel,
.add-problem-btn,
.retry-btn,
.actions button {
  padding: 6px 14px;
  font-size: 14px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.2s;
}

.btn-ok,
.retry-btn,
.actions button,
.add-problem-btn {
  background: #007bff;
  color: #fff;
}

.btn-ok:hover,
.retry-btn:hover,
.actions button:hover:not(:disabled),
.add-problem-btn:hover {
  background: #0056b3;
}

.btn-cancel {
  background: #6c757d;
  color: #fff;
  margin-left: 8px;
}

.btn-cancel:hover {
  background: #545b62;
}

.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal {
  background: #fff;
  padding: 24px 32px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
  text-align: center;
}

.modal-actions {
  margin-top: 16px;
  display: flex;
  gap: 12px;
  justify-content: center;
}

.btn-delete {
  background: #e74c3c;
  color: #fff;
  border: none;
  border-radius: 4px;
  padding: 4px 8px;
  font-size: 12px;
  cursor: pointer;
}

.btn-delete:hover {
  background: #c0392b;
}

/* ========== Leaderboard 样式（新版） ========== */
.board {
  border: 1px solid var(--border);
  border-radius: var(--radius);
  overflow: hidden;
  box-shadow: var(--shadow);
  background: #fff;
}

/* 通用网格：列数由 --cols 控制（= 题目数 + 2 列：总分、最后提交 + 1 列用户） */
.board-header,
.board-body {
  display: grid;
  grid-template-columns: 220px repeat(calc(var(--cols) - 2), minmax(90px, 1fr)) 120px 160px;
}

.cell {
  word-break: break-word;
  white-space: normal; /* ✅ 自动换行 */
  padding: 8px;
  font-size: 14px;
}

.cell.th {
  background: #f8fafc;
  font-weight: 600;
  color: #334155;
  position: sticky;
  top: 0;
  z-index: 2;
  border-bottom: 1px solid var(--border);
}

.board-body .board-row:nth-child(even) .cell {
  background: #fbfdff; /* 斑马纹 */
}
.board-body .board-row:hover .cell {
  background: #f2f7ff; /* 悬停高亮 */
}

.board-header,
.board-body {
  display: grid;
  grid-template-columns: 1.2fr repeat(calc(var(--cols) - 3), 1fr) 0.8fr 1fr; 
  /* ✅ 用户列更宽，题目列平均，最后两列略小 */
  width: 100%; /* ✅ 占满父容器 */
}

/* 左侧第一列（用户）粘性，避免横向滚动看不见用户名 */
.left-sticky {
  position: sticky;
  left: 0;
  z-index: 3;
  justify-content: flex-start;
  gap: 10px;
}

/* 用户单元格：排行 + 头像 + 名称 */
.user-cell {
  padding-left: 14px;
}

.rank {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: #e5e7eb;
  color: #111827;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 12px;
}
.rank.gold   { background: #fde68a; color: #8a5800; }   /* 金牌 */
.rank.silver { background: #e5e7eb; color: #4b5563; }   /* 银牌 */
.rank.bronze { background: #fcd5b5; color: #7b3f00; }   /* 铜牌 */

.avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: #dbeafe;
  color: #1e40af;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 12px;
}

.uname {
  max-width: 140px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 分数样式（原规则；下面覆盖为黑色显示） */
.score {
  font-variant-numeric: tabular-nums;
  color: #6b7280;
}
.score.pass {
  color: #0a7b3e;
  font-weight: 600;
}

/* 总分：进度条 + 数字 */
.total {
  gap: 10px;
}
.total .bar {
  flex: 1;
  height: 8px;
  background: #eef2ff;
  border-radius: 999px;
  overflow: hidden;
}
.total .bar-inner {
  height: 100%;
  background: linear-gradient(90deg, #60a5fa, #2563eb);
  border-radius: 999px;
}
.total .total-text {
  width: 42px;
  text-align: right;
  font-variant-numeric: tabular-nums;
  color: #111827;
  font-weight: 600;
}

/* 响应式微调：窄屏时压缩列宽 */
@media (max-width: 900px) {
  .board-header,
  .board-body {
    grid-template-columns: 200px repeat(calc(var(--cols) - 2), minmax(72px, 1fr)) 110px 140px;
  }
  .uname { max-width: 110px; }
}
@media (max-width: 720px) {
  .board-header,
  .board-body {
    grid-template-columns: 180px repeat(calc(var(--cols) - 2), minmax(64px, 1fr)) 100px 120px;
  }
  .uname { max-width: 96px; }
}

/* ====== 覆盖：每题得分填充 + 黑色文字（放在末尾以确保覆盖） ====== */
.cell.score { color: #111827; }
.cell.score .score-text { color: #111827; }
.score.pass { color: #111827 !important; }

.info-item {
  display: flex;
  align-items: flex-start; /* 顶部对齐 */
  gap: 8px; /* 标签和内容之间的间距 */
  margin: 4px 0;
  font-size: 14px;
}

.note-content {
  flex: 1; /* 让内容占据剩余空间 */
  margin-top: -2px; /* 微调，让内容与“备注”标签的基线对齐 */
}

</style>