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

        <div v-if="problemsLoading" class="status">正在加载题目...</div>
        <div v-else-if="problemsError" class="status error">
          加载题目失败：{{ problemsError }}
          <button @click="fetchProblemList" class="retry-btn">重试</button>
        </div>
        <ul v-else class="problem-list">
          <li v-if="problemList.length === 0" class="empty">当前没有题目</li>
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
          </li>
        </ul>
      </section>

      <!-- 榜单 -->
      <section class="leaderboard">
        <div class="section-header">
          <h3>榜单</h3>
          <button @click="viewLeaderboard">查看完整榜单</button>
        </div>
        <div class="placeholder">
          <p>榜单内容暂未实现（可以做成弹出/内嵌/跳转）。</p>
        </div>
      </section>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ContestDetail',
  props: {
    contest: {
      type: Object,
      required: true,
    },
  },
  data() {
    return {
      fullContest: {}, // 详尽信息
      loading: false,
      error: null,
      problemList: [],
      problemsLoading: false,
      problemsError: null,
      attended: false,
      attending: false,
      countdown: '',
      timer: null,
    }
  },
  computed: {
    effectiveContest() {
      // 以后端获取的详尽为准，没有则 fallback 传入的
      return Object.keys(this.fullContest).length ? this.fullContest : this.contest || {}
    },
    inAssessment() {
      const c = this.effectiveContest
      if (!c.start || !c.end) return false
      const now = Date.now()
      const s = new Date(c.start).getTime()
      const e = new Date(c.end).getTime()
      return now >= s && now <= e
    },
    beforeStart() {
      const c = this.effectiveContest
      if (!c.start) return false
      return Date.now() < new Date(c.start).getTime()
    },
    remainingText() {
      const c = this.effectiveContest
      if (!c.start) return ''
      const delta = Math.max(0, new Date(c.start).getTime() - Date.now())
      const sec = Math.floor(delta / 1000)
      const m = Math.floor(sec / 60)
      const s = sec % 60
      return `还有 ${m}m${s}s 开始`
    },
  },
  created() {
    // guard：必须有有效 contest.name
    if (!this.contest || !this.contest.name) {
      const msg = 'ContestDetail 初始化失败：未传有效 contest prop'
      this.error = msg
      alert(msg)
      console.error(msg, this.contest)
      return
    }
    // 先用简略数据填充
    this.fullContest = { ...this.contest }
    this.fetchFullContest()
    this.fetchProblemList()
    this.startCountdownIfNeeded()
  },
  beforeUnmount() {
    if (this.timer) clearInterval(this.timer)
  },
  methods: {
    formatTime(ts) {
      if (!ts) return ''
      const d = new Date(ts)
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(
        d.getDate()
      ).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(
        d.getMinutes()
      ).padStart(2, '0')}`
    },
    async fetchFullContest() {
      this.loading = true
      this.error = null
      try {
        const token = localStorage.getItem('jwt') || ''
        const res = await fetch(`/contest/full/getContest`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            Authorization: `Bearer ${token}`,
          },
          body: JSON.stringify({
            dto: {
              name: this.contest.name,
              data: {},
            },
          }),
        })
        const text = await res.text()
        if (!res.ok) {
          this.error = `获取详情失败：HTTP ${res.status}`
          console.warn('getContest 非 2xx 返回：', text)
          return
        }
        if (text.trim().startsWith('<')) {
          this.error = '获取详情返回 HTML，可能未登录或路径错'
          console.warn('getContest 返回 HTML:', text.slice(0, 500))
          return
        }
        const json = JSON.parse(text)
        if ((json.status === 1 || json.status === 0) && json.data) {
          this.fullContest = json.data
        } else {
          this.error = json.error || '详情接口返回异常'
        }
      } catch (e) {
        this.error = e.message || '获取详情失败'
      } finally {
        this.loading = false
      }
    },
    async fetchProblemList() {
      this.problemsLoading = true
      this.problemsError = null
      try {
        const token = localStorage.getItem('jwt') || ''
        const res = await fetch(`/contest/list/problem`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            Authorization: `Bearer ${token}`,
            contestName: this.contest.name, // header 里传
          },
          body: JSON.stringify({}),
        })
        const text = await res.text()
        if (!res.ok) {
          this.problemsError = `获取题目失败：HTTP ${res.status}`
          console.warn('getProblemList 非 2xx 返回：', text)
          return
        }
        if (text.trim().startsWith('<')) {
          this.problemsError = '题目列表接口返回 HTML，可能权限/登录问题'
          console.warn('getProblemList 返回 HTML:', text.slice(0, 500))
          return
        }
        const json = JSON.parse(text)
        if ((json.status === 1 || json.status === 0) && Array.isArray(json.data)) {
          this.problemList = json.data
        } else {
          this.problemsError = json.error || '题目列表数据异常'
        }
      } catch (e) {
        this.problemsError = e.message || '获取题目失败'
      } finally {
        this.problemsLoading = false
      }
    },
    startCountdownIfNeeded() {
      if (this.beforeStart) {
        this.updateCountdown()
        this.timer = setInterval(this.updateCountdown, 1000)
      }
    },
    updateCountdown() {
      if (!this.effectiveContest.start) return
      const delta = Math.max(0, new Date(this.effectiveContest.start).getTime() - Date.now())
      const sec = Math.floor(delta / 1000)
      const m = Math.floor(sec / 60)
      const s = sec % 60
      this.countdown = `${m}m${s}s`
      if (delta <= 0 && this.timer) {
        clearInterval(this.timer)
        this.countdown = ''
      }
    },
    goToProblem(p) {
      this.$router.push({
        path: '/contest/answer',
        query: { contest: this.effectiveContest.name, problem: p.problemName },
      })
    },
    async handleAttend() {
      if (this.attended) return
      this.attending = true
      try {
        const token = localStorage.getItem('jwt') || ''
        const body = {
          cnname: this.fullContest.cnname || this.fullContest.name || '',
          contestName: this.contest.name,
        }
        const res = await fetch(`/contest/attend`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            Authorization: `Bearer ${token}`,
          },
          body: JSON.stringify(body),
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
          this.attended = true
        } else {
          alert(`参加失败：${json.error || '未知'}`)
        }
      } catch (e) {
        console.error('参加出错', e)
        alert(`参加出错：${e.message}`)
      } finally {
        this.attending = false
      }
    },
    viewLeaderboard() {
      this.$emit('view-leaderboard', { contest: this.effectiveContest })
    },
  },
}
</script>

<style scoped>
.contest-detail {
  padding: 16px;
  max-width: 1000px;
  margin: 0 auto;
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 16px;
}
.left {
  flex: 1;
}
.time-range {
  font-size: 0.9em;
  margin-top: 4px;
  display: flex;
  gap: 8px;
  align-items: center;
}
.status {
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 0.75em;
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
.actions button {
  margin-left: 8px;
  padding: 8px 14px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  background: #3b82f6;
  color: white;
}
.actions button[disabled] {
  background: #ccc;
  cursor: not-allowed;
}
.info {
  margin-bottom: 20px;
}
.problems {
  margin-top: 16px;
}
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.problem-list {
  list-style: none;
  padding: 0;
  margin: 8px 0;
}
.problem-item {
  padding: 12px;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  margin-bottom: 8px;
  display: flex;
  cursor: pointer;
}
.problem-item .left {
  flex: 1;
}
.problem-item .title {
  font-weight: bold;
}
.meta {
  font-size: 0.85em;
  color: #555;
  margin-top: 4px;
}
.leaderboard {
  margin-top: 32px;
}
.placeholder {
  background: #f3f4f6;
  padding: 14px;
  border-radius: 6px;
  margin-top: 8px;
}
.status.error {
  color: #b91c1c;
}
.empty {
  padding: 10px;
  color: #666;
}
.subnote {
  font-size: 0.85em;
  color: #555;
}
.retry-btn {
  margin-left: 8px;
  padding: 4px 10px;
  border: none;
  background: #f59e0b;
  color: white;
  border-radius: 4px;
  cursor: pointer;
}
</style>
