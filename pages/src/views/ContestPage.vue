<template>
  <main-layout selected-tab="contest">
    <div class="contest-list">
      <!-- 顶部标题栏 -->
      <div class="header">
        <h2>竞赛考核</h2>
        <div class="actions">
          <el-button v-if="isAdmin" type="primary" @click="createContest">
            新建竞赛
          </el-button>
        </div>
      </div>

      <!-- 加载/错误提示 -->
      <div v-if="loading" class="status">正在加载竞赛列表...</div>
      <div v-else-if="error" class="status error">加载失败：{{ error }}</div>

      <!-- 竞赛列表 -->
      <ul v-else class="contest-items">
        <li v-if="contests.length === 0" class="empty">暂无竞赛</li>
        <li v-for="c in contests" :key="c.name" class="contest-item">
          <div class="info" @click="openDetail(c)">
            <div class="name">{{ c.cnname || c.name }}</div>
            <div class="meta">
              <span>{{ formatTime(c.start) }} - {{ formatTime(c.end) }}</span>
              <el-tag v-if="inAssessment(c)" type="success">进行中</el-tag>
              <el-tag v-else-if="beforeStart(c)" type="warning">未开始</el-tag>
              <el-tag v-else type="info">已结束</el-tag>
            </div>
            <div class="meta">
              <span>模式：{{ c.mode }}</span>
              <span>公开：{{ c.ispublic ? '是' : '否' }}</span>
            </div>
            <div v-if="c.note" class="note">备注：{{ c.note }}</div>
            <div v-if="beforeStart(c)" class="countdown">
              倒计时：{{ countdowns[c.name] }}
            </div>
          </div>
          <div class="ops">
            <el-button v-if="isAdmin" size="small" @click.stop="editContest(c)">
              修改
            </el-button>
          </div>
        </li>
      </ul>
    </div>
  </main-layout>
</template>

<script>
import MainLayout from '@/components/MainLayout.vue'
import { ElButton, ElTag } from 'element-plus'
import { defineConfig } from 'vite'

export default {
  server: {
    proxy: {
      '/contest': {
        target: 'http://letucoj.cn:7777',
        changeOrigin: true,
        rewrite: path => path, // 不修改路径
      },
    },
  },
  name: 'ContestView',
  components: { MainLayout, ElButton, ElTag },
  data() {
    return {
      contests: [],
      loading: false,
      error: null,
      countdowns: {},
      timerHandles: {},
      userRole: null,
    }
  },
  computed: {
    isAdmin() {
      return this.userRole === 'MANAGER' || this.userRole === 'ROOT'
    },
  },
  created() {
    this.parseToken()
    this.fetchList()
  },
  beforeUnmount() {
    Object.values(this.timerHandles).forEach(clearInterval)
  },
  methods: {
    // 解析 JWT 获取用户角色
    parseToken() {
      const token = localStorage.getItem('jwt') || ''
      try {
        const [, base64] = token.split('.')
        if (!base64) return
        const payload = JSON.parse(
          decodeURIComponent(
            atob(base64.replace(/-/g, '+').replace(/_/g, '/'))
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

    // 获取竞赛列表
    async fetchList() {
      this.loading = true
      this.error = null
      try {
        const token = localStorage.getItem('jwt') || ''
        const res = await fetch('contest/list/contest', {
          method: 'GET',
          headers: {
            Authorization: `Bearer ${token}`,
            Accept: 'application/json',
          },
        })
        const text = await res.text()
        if (!res.ok || text.trim().startsWith('<')) {
          throw new Error('接口返回异常')
        }
        const json = JSON.parse(text)
        if ((json.status === 0 || json.status === 1) && Array.isArray(json.data)) {
          this.contests = json.data
          this.contests.forEach((c) => {
            if (this.beforeStart(c)) this.startCountdown(c)
          })
        } else {
          throw new Error(json.error || '返回格式异常')
        }
      } catch (e) {
        this.error = e.message || '请求失败'
      } finally {
        this.loading = false
      }
    },


    // 格式化时间
    formatTime(ts) {
      const d = new Date(ts)
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(
        d.getDate()
      ).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(
        d.getMinutes()
      ).padStart(2, '0')}`
    },

    // 时间判断
    beforeStart(c) {
      return new Date(c.start).getTime() > Date.now()
    },
    inAssessment(c) {
      const now = Date.now()
      const s = new Date(c.start).getTime()
      const e = new Date(c.end).getTime()
      return now >= s && now <= e
    },

    // 倒计时
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

    // 跳转
    openDetail(c) {
      this.$router.push({ name: 'contest-detail', params: { name: c.name } })
    },
    createContest() {
      this.$router.push({ path: '/contests/manage' })
    },
    editContest(c) {
      this.$router.push({ path: '/contests/manage', query: { name: c.name } })
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
.note {
  margin-top: 4px;
  font-size: 0.85em;
  color: #777;
}
.countdown {
  margin-top: 6px;
  font-size: 0.9em;
  color: #1f2937;
}
.ops {
  display: flex;
  gap: 4px;
}
.empty,
.status {
  padding: 20px;
  color: #777;
}
.status.error {
  color: #b91c1c;
}
</style>
