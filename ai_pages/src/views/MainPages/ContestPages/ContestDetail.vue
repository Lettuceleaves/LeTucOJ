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
        <button @click="$emit('close')">退出</button>
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
        <p><strong>备注：</strong>{{ effectiveContest.note || '无' }}</p>
        <p><strong>公开：</strong>{{ effectiveContest.ispublic ? '是' : '否' }}</p>
      </section>

      <!-- 题目列表 -->
      <section class="problems">
        <div class="section-header">
          <h3>题目列表</h3>
          <div class="subnote" v-if="!inAssessment">
            目前不是考核时间，题目不可作答。{{ remainingText }}
          </div>
        </div>

        <!-- 加载中 / 错误 -->
        <div v-if="problemsLoading" class="status">正在加载题目...</div>
        <div v-else-if="problemsError && !problemsError.includes('No problems found')" class="status error">
          加载题目失败：{{ problemsError }}
          <button @click="fetchProblemList" class="retry-btn">重试</button>
        </div>

        <!-- 正常区域 -->
        <template v-else>
          <!-- 题目列表 -->
          <ul class="problem-list">
            <!-- 题目列表 -->
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
              <div v-if="canDelete" class="right">
                <button
                  v-if="canDelete"
                  class="btn-delete"
                  @click.stop="openDeleteConfirm(p.problemName)"
                >
                  删除
                </button>
              </div>
            </li>

            <!-- 空列表提示 -->
            <li v-if="problemList.length === 0 && !isAddingProblem" class="empty">
              当前没有题目
            </li>

            <!-- 新增输入框（作为最后一项） -->
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


            <!-- 添加按钮（作为最后一项） -->
            <li
              v-if="canAddProblem && !isAddingProblem"
              class="problem-item add-button-row"
              @click="startAddProblem"
            >
              <button class="add-problem-btn">+ 添加题目</button>
            </li>
          </ul>
        </template>
      </section>

      <!-- 榜单 -->
      <section class="leaderboard">
        <div class="section-header"><h3>榜单</h3></div>

        <div v-if="leaderboardData.length === 0" class="placeholder">
          <p>暂无提交记录</p>
        </div>

        <!-- 动态列数 + 80% 宽 + 正方形单元格 -->
        <div
          v-else
          class="board-wrapper"
          :style="{ '--cols': colCount }"
        >
          <!-- 表头 -->
          <div class="board-row header-row">
            <div class="cell">用户</div>
            <div class="cell" v-for="p in uniqueProblems" :key="p">{{ p }}</div>
            <div class="cell">总分</div>
            <div class="cell">最后提交</div>
          </div>

          <!-- 表体 -->
          <div
            class="board-row"
            v-for="(row, user) in groupedByUser"
            :key="user"
          >
            <div class="cell">{{ row.userCnname || user }}</div>
            <div class="cell" v-for="p in uniqueProblems" :key="p">
              {{ row.scores[p] ?? 0 }}
            </div>
            <div class="cell">{{ row.totalScore }}</div>
            <div class="cell">{{ formatTime(row.lastSubmit) }}</div>
          </div>
        </div>
      </section>


    </div>
  </div>

  <!-- 删除确认弹窗 -->
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
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const ip = import.meta.env.VITE_API_IP || 'localhost:7777'

const route = useRoute()
const router = useRouter()
const ctname = route.query.ctname

const isAddingProblem = ref(false)
const newProblemName = ref('')

const canDelete = computed(() => ['ROOT', 'MANAGER'].includes(userRole.value))

const userRole = ref('')

function parseRoleFromJWT() {
  const token = localStorage.getItem('jwt')
  if (!token) return
  try {
    const payload = JSON.parse(atob(token.split('.')[1]))
    userRole.value = payload.role || ''
  } catch (e) {
    console.error('解析 JWT 失败', e)
  }
}


// 确保传递 ctname 给 props
const props = defineProps({
  contest: {
    type: Object,
    required: true
  }
})

// 在 fetchFullContest 和 fetchProblemList 方法中使用 ctname
console.log(ctname) // 测试是否正确获取到参数


/* emits */
const emit = defineEmits(['close', 'view-leaderboard'])

/* reactive data */
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

const newProblemScore = ref(100) // 默认分数为 100

const deleteConfirmVisible = ref(false)
const deleteTargetName = ref('')

function openDeleteConfirm(name) {
  deleteTargetName.value = name
  deleteConfirmVisible.value = true
}

function closeDeleteConfirm() {
  deleteConfirmVisible.value = false
  deleteTargetName.value = ''
}

const uniqueProblems = computed(() => {
  const set = new Set(leaderboardData.value.map(r => r.problemName))
  return Array.from(set)
})

const colCount = computed(() => uniqueProblems.value.length + 3)

const groupedByUser = computed(() => {
  const map = {}
  for (const r of leaderboardData.value) {
    if (!map[r.userName]) {
      map[r.userName] = {
        userCnname: r.userCnname,
        scores: {},
        lastSubmit: r.lastSubmit,
        totalScore: 0
      }
    }
    map[r.userName].scores[r.problemName] = r.score
    map[r.userName].totalScore += r.score
    if (new Date(r.lastSubmit) > new Date(map[r.userName].lastSubmit)) {
      map[r.userName].lastSubmit = r.lastSubmit
    }
  }
  return map
})

const leaderboardData = ref([])

async function fetchLeaderboard() {
  try {
    const token = localStorage.getItem('jwt')
    const params = new URLSearchParams({
      ctname: ctname
    })
    const res = await fetch(`http://${ip}/contest/list/board?${params}`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`
      }
    })
    const text = await res.text()
    if (!res.ok) {
      console.warn('获取榜单失败：HTTP', res.status)
      return
    }
    const json = JSON.parse(text)
    if ((json.status === 0) && Array.isArray(json.data)) {
      leaderboardData.value = json.data
    } else if (json.status === 1) {
      leaderboardData.value = []
      alert('榜单数据为空或未开始竞赛')
    } else {
      alert('获取榜单失败：' + (json.error || '未知错误'))
    }

  } catch (e) {
    console.error('获取榜单失败', e)
  }
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
        problemName: deleteTargetName.value,
        score: newProblemScore.value
      })
    })
    const json = await res.json()
    if (json.status === 1 || json.status === 0) {
      await fetchProblemList()
      closeDeleteConfirm()
    } else {
      alert(`删除失败：${json.error || '未知错误'}`)
    }
  } catch (e) {
    alert(`删除失败：${e.message}`)
  }
}

const canAddProblem = computed(() => {
  return ['ROOT', 'MANAGER'].includes(userRole.value)
})

function startAddProblem() {
  isAddingProblem.value = true
  newProblemName.value = ''
}

function cancelAddProblem() {
  isAddingProblem.value = false
  newProblemName.value = ''
}

async function confirmAddProblem() {
  const name = newProblemName.value.trim()
  if (!name) {
    alert('请输入题目名称')
    return
  }

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
    if (json.status === 0) {
      // 成功
      await fetchProblemList()
      cancelAddProblem()
    } else if (json.status === 1) {
      // 题目不存在，可继续新增
      alert(json.message || '题目未在题库中，请先在题库创建后再添加')
    } else {
      alert(json.error || '添加失败')
    }
  } catch (e) {
    alert(`添加失败：${e.message}`)
  }
}

/* computed */
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
  const c = effectiveContest.value
  if (!c.start) return ''
  const delta = Math.max(0, new Date(c.start).getTime() - Date.now())
  const sec = Math.floor(delta / 1000)
  const m = Math.floor(sec / 60)
  const s = sec % 60
  return `还有 ${m}m${s}s 开始`
})

/* methods */
function formatTime(ts) {
  if (!ts) return ''
  const d = new Date(ts)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(
    d.getDate()
  ).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(
    d.getMinutes()
  ).padStart(2, '0')}`
}

async function fetchFullContest() {
  loading.value = true
  error.value = null

  const params = new URLSearchParams({
    ctname: ctname
  });
  try {
    const token = localStorage.getItem('jwt') || ''
    const res = await fetch(`http://${ip}/contest/full/getContest?${params}`, { // 添加 http:// 和 ip
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`
      }
    })
    const text = await res.text()
    if (!res.ok) {
      error.value = `获取详情失败：HTTP ${res.status}`
      console.warn('getContest 非 2xx 返回：', text)
      return
    }
    if (text.trim().startsWith('<')) {
      error.value = '获取详情返回 HTML，可能未登录或路径错'
      console.warn('getContest 返回 HTML:', text.slice(0, 500))
      return
    }
    const json = JSON.parse(text)
    if ((json.status === 1 || json.status === 0) && json.data) {
      fullContest.value = json.data
    } else {
      error.value = json.error || '详情接口返回异常'
    }
  } catch (e) {
    error.value = e.message || '获取详情失败'
  } finally {
    loading.value = false
  }
}

async function fetchProblemList() {
  problemsLoading.value = true
  problemsError.value = null
  try {
    const token = localStorage.getItem('jwt') || ''
    const params = new URLSearchParams({
      ctname: ctname
    });

    const res = await fetch(`http://${ip}/contest/list/problem?${params}`, { // 添加 http:// 和 ip
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`
      }
    })
    const text = await res.text()
    if (!res.ok) {
      problemsError.value = `获取题目失败：HTTP ${res.status}`
      console.warn('getProblemList 非 2xx 返回：', text)
      return
    }
    if (text.trim().startsWith('<')) {
      problemsError.value = '题目列表接口返回 HTML，可能权限/登录问题'
      console.warn('getProblemList 返回 HTML:', text.slice(0, 500))
      return
    }
    const json = JSON.parse(text)
    if ((json.status === 1 || json.status === 0) && Array.isArray(json.data)) {
      problemList.value = json.data
    } else {
      problemsError.value = json.error || '题目列表数据异常'
    }
  } catch (e) {
    problemsError.value = e.message || '获取题目失败'
  } finally {
    problemsLoading.value = false
  }
}

function startCountdownIfNeeded() {
  if (beforeStart.value) {
    updateCountdown()
    timer = setInterval(updateCountdown, 1000)
  }
}

function updateCountdown() {
  if (!effectiveContest.value.start) return
  const delta = Math.max(0, new Date(effectiveContest.value.start).getTime() - Date.now())
  const sec = Math.floor(delta / 1000)
  const m = Math.floor(sec / 60)
  const s = sec % 60
  countdown.value = `${m}m${s}s`
  if (delta <= 0 && timer) {
    clearInterval(timer)
    countdown.value = ''
  }
}

function goToProblem(p) {
  router.push({
    path: '/contest/editor',
    query: { contest: effectiveContest.value.name, problem: p.problemName }
  })
}

async function handleAttend() {
  if (attended.value) return
  attending.value = true
  try {
    const token = localStorage.getItem('jwt') || ''
    const params = new URLSearchParams({
      ctname: ctname
    });
    const res = await fetch(`http://${ip}/contest/attend?${params}`, {
      method: 'POST',
      headers: {
        Authorization: `Bearer ${token}`
      }
    })
    const text = await res.text()
    if (!res.ok) {
      console.warn('attendContest 非 2xx 返回：', text)
      alert(`参加失败：HTTP ${res.status}`)
      return
    }
    if (text.trim().startsWith('<')) {
      console.warn('attendContest 返回 HTML:', text.slice(0, 500))
      alert('参加竞赛时返回了非 JSON，可能未登录')
      return
    }
    const json = JSON.parse(text)
    if (json.status === 1 || json.status === 0) {
      attended.value = true
    } else {
      alert(`参加失败：${json.error || '未知'}`)
    }
  } catch (e) {
    console.error('参加出错', e)
    alert(`参加出错：${e.message}`)
  } finally {
    attending.value = false
  }
}

async function fetchAttendStatus() {
  try {
    const token = localStorage.getItem('jwt')
    const res = await fetch(`http://${ip}/contest/inContest?ctname=${ctname}`, {
      headers: { Authorization: `Bearer ${token}` }
    })
    const json = await res.json()
    if (json.status === 0) {
      attended.value = json.data
    } else if (json.status === 1) {
      attended.value = false
    } else {
      console.error('获取参加状态失败', json.error)
    }
  } catch (e) {
    console.error('获取参加状态失败', e)
  }
}

/* lifecycle */
onMounted(async () => {
  parseRoleFromJWT()
  fetchFullContest()
  fetchProblemList()
  fetchAttendStatus()
  fetchLeaderboard()
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
  --primary: #28a745;          /* 主色改为绿色 */
  --primary-hover: #1e7e34;
  --danger: #e74c3c;
  --success: #007bff;          /* 互换：确定按钮用蓝 */
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

</style>
