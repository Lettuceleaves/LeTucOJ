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
      <li v-if="contests.length === 0" class="empty">暂无竞赛</li>
      <li v-for="c in contests" :key="c.name" class="contest-item">
        <div class="info" @click="openDetail(c)">
          <div class="name">{{ c.cnname }}（{{ c.name }}）</div>
          <div class="meta">
            <span>{{ formatTime(c.start) }} - {{ formatTime(c.end) }}</span>
            <span v-if="inAssessment(c)" class="badge ongoing">进行中</span>
            <span v-else-if="beforeStart(c)" class="badge upcoming">未开始</span>
            <span v-else class="badge ended">已结束</span>
          </div>
          <div v-if="beforeStart(c)" class="countdown">
            倒计时：{{ getRemaining(c) }}
          </div>
        </div>
        <div class="ops">
          <button v-if="isAdmin" @click.stop="editContest(c)">修改</button>
        </div>
      </li>
    </ul>

    <!-- 详情可以内嵌，也可以 emit 给上层 -->
    <div v-if="selected" class="detail-panel">
      <h3>选中：{{ selected.cnname }}（{{ selected.name }}）</h3>
      <div>
        <!-- 这里可以再插入题目列表组件 / 详情组件 -->
        <p>备注：{{ selected.note }}</p>
        <p>公开：{{ selected.ispublic ? '是' : '否' }}</p>
      </div>
      <button @click="selected = null">关闭</button>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Contest',
  data() {
    return {
      contests: [],
      loading: false,
      error: null,
      selected: null,
      countdowns: {},
      timerHandles: {},
      userRole: null,
    }
  },
  computed: {
    isAdmin() {
      return this.userRole === 'MANAGER' || this.userRole === 'ROOT'
    },
    now() {
      return Date.now()
    },
  },
  created() {
    this.parseToken()
    this.fetchList()
  },
  beforeUnmount() {
    // 清理倒计时
    Object.values(this.timerHandles).forEach(clearInterval)
  },
  methods: {
    parseToken() {
      const token = localStorage.getItem('jwt') || ''
      try {
        const base64 = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')
        const payload = JSON.parse(
          decodeURIComponent(
            atob(base64)
              .split('')
              .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
              .join('')
          )
        )
        this.userRole = payload.role
      } catch (e) {
        console.warn('JWT 解析失败', e)
      }
    },
    async fetchList() {
        this.loading = true
        this.error = null
        try {
            const token = localStorage.getItem('jwt') || ''
            const res = await fetch(`http://localhost:7777/contest/list/contest`, { // <-- 确保加上协议+端口
            method: 'GET',
            headers: {
                Authorization: `Bearer ${token}`,
                'Accept': 'application/json',
            },
            })

            const text = await res.text()

            // 如果返回的根字符是 HTML（常见 <!DOCTYPE），说明请求被重定向或出错
            if (text.trim().startsWith('<')) {
            console.warn('非 JSON 响应，原始内容：', text.slice(0, 1000))
            this.error = '接口返回 HTML，可能是路径/权限/后端错误，详见控制台'
            return
            }

            // 解析 JSON
            const json = JSON.parse(text)

            if ((json.status === 1 || json.status === 0) && Array.isArray(json.data)) {
            this.contests = json.data
            this.contests.forEach((c) => {
                if (this.beforeStart(c)) {
                this.startCountdown(c)
                }
            })
            } else {
            this.error = json.error || '返回格式异常'
            }
        } catch (e) {
            console.error('fetchList 失败，原始错误:', e)
            this.error = e.message || '请求失败'
        } finally {
            this.loading = false
        }
        },
    formatTime(ts) {
      const d = new Date(ts)
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(
        d.getDate()
      ).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(
        d.getMinutes()
      ).padStart(2, '0')}`
    },
    beforeStart(c) {
      return new Date(c.start).getTime() > Date.now()
    },
    inAssessment(c) {
      const now = Date.now()
      const s = new Date(c.start).getTime()
      const e = new Date(c.end).getTime()
      return now >= s && now <= e
    },
    getRemaining(c) {
      const delta = Math.max(0, new Date(c.start).getTime() - Date.now())
      const sec = Math.floor(delta / 1000)
      const m = Math.floor(sec / 60)
      const s = sec % 60
      return `${m}m${s}s`
    },
    startCountdown(c) {
      this.countdowns[c.name] = this.getRemaining(c)
      this.timerHandles[c.name] = setInterval(() => {
        this.countdowns[c.name] = this.getRemaining(c)
        if (!this.beforeStart(c)) {
          clearInterval(this.timerHandles[c.name])
        }
      }, 1000)
    },
    openDetail(c) {
      this.selected = c
    },
    editContest(c) {
      // 带参数跳管理页
      this.$router.push({ path: '/contest/manage', query: { name: c.name } })
    },
    createContest() {
      this.$router.push({ path: '/contest/manage' })
    },
  },
}
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
.detail-panel {
  margin-top: 20px;
  padding: 16px;
  border: 1px solid #d1d5e0;
  border-radius: 8px;
  background: #f9fafb;
}
</style>
