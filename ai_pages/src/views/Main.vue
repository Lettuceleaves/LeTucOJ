<!-- ContestParent.vue -->
<template>
  <div class="contest-parent">
    <!-- 标题栏 -->
    <div class="title-bar">
      <div
        class="title-item"
        :class="{ active: activeTab === 'list' }"
        @click="switchTab('list')"
      >题单</div>

      <div
        class="title-item"
        :class="{ active: activeTab === 'contest' }"
        @click="switchTab('contest')"
      >竞赛</div>

      <div
        class="title-item"
        :class="{ active: activeTab === 'rank' }"
        @click="switchTab('rank')"
      >排行榜</div>

      <div
        class="title-item"
        :class="{ active: activeTab === 'profile' }"
        @click="switchTab('profile')"
      >个人</div>

      <!-- 仅管理员可见 -->
      <div
        v-if="showUserTab"
        class="title-item"
        :class="{ active: activeTab === 'user' }"
        @click="switchTab('user')"
      >管理</div>

      <!-- 中间圆角状态框 -->
      <div
        v-if="isStatusVisible"
        class="nav-status"
        :class="{
          'status-loading': loadStatus === 'loading',
          'status-success': loadStatus === 'success',
          'status-error': loadStatus === 'error'
        }"
        @click="hideStatus"
        title="编辑器加载状态（失败可点击重试）"
      >
        <!-- 3. 关键：让 span 不响应鼠标 -->
        <span style="pointer-events:none;">
          <template v-if="loadStatus === 'loading'">
            编辑器组件加载中，请加载完毕后再点击列表项
          </template>
          <template v-else-if="loadStatus === 'success'">加载成功</template>
          <template v-else>加载失败</template>
        </span>
      </div>

      <div class="spacer"></div>
      <button class="back-btn" @click="goBack">返回</button>
    </div>

    <!-- 内容区域 -->
    <div class="content">
      <Suspense>
        <template #default>
          <KeepAlive>
            <component
              :is="currentComp"
              :key="activeTab"
              class="tab-pane"
              :editor-ready="editorReady"
            />
          </KeepAlive>
        </template>
        <template #fallback>
          <div class="skeleton">
            <div class="skeleton-line w-60"></div>
            <div class="skeleton-line w-90"></div>
            <div class="skeleton-line w-80"></div>
            <div class="skeleton-line w-75"></div>
            <div class="skeleton-line w-85"></div>
          </div>
        </template>
      </Suspense>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick, defineAsyncComponent, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

/* ---------- 1. 解析 JWT 拿到角色 ---------- */
const parseJwt = (tk) => {
  try {
    const base64Url = tk.split('.')[1]
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/')
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split('')
        .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    )
    return JSON.parse(jsonPayload)
  } catch {
    return {}
  }
}
const token = () => localStorage.getItem('jwt') || ''
const role = ref(parseJwt(token()).role || '')

/* ---------- 2. 是否显示「用户」Tab ---------- */
const showUserTab = computed(() => role.value === 'ROOT' || role.value === 'MANAGER')

/* ---------- 3. 标签页初始值 ---------- */
const initialTab = (typeof route.query.tab === 'string' ? route.query.tab : 'list')
const activeTab = ref(['list', 'contest', 'rank', 'user', 'profile'].includes(initialTab) ? initialTab : 'list')

/* ---------- 4. 异步组件 ---------- */
const AsyncProblemList = defineAsyncComponent(() =>
  import(/* webpackChunkName: "tab-problem-list" */ './MainPages/List.vue')
)
const AsyncContest = defineAsyncComponent(() =>
  import(/* webpackChunkName: "tab-contest" */ './MainPages/Contest.vue')
)
const AsyncRank = defineAsyncComponent(() =>
  import(/* webpackChunkName: "tab-rank" */ './MainPages/Rank.vue')
)
const AsyncUser = defineAsyncComponent(() =>
  import(/* webpackChunkName: "tab-user" */ '../views/ManagePage.vue')
)
const AsyncProfile = defineAsyncComponent(() =>
  import(/* webpackChunkName: "tab-profile" */ './MainPages/User.vue')
)
const compMap = { list: AsyncProblemList, contest: AsyncContest, rank: AsyncRank, user: AsyncUser, profile: AsyncProfile }
const currentComp = computed(() => compMap[activeTab.value])

/* ---------- 5. 切换标签 + URL 同步 ---------- */
function switchTab(tab) {
  if (tab === activeTab.value) return
  activeTab.value = tab
}
watch(activeTab, (val) => {
  router.replace({ path: route.path, query: { ...route.query, tab: val } })
})
watch(() => route.query.tab, (val) => {
  if (typeof val === 'string' && ['list', 'contest', 'rank', 'user'].includes(val)) {
    if (val !== activeTab.value) activeTab.value = val
  }
})

/* ---------- 6. 权限兜底 ---------- */
watch(showUserTab, (visible) => {
  if (!visible && activeTab.value === 'user') switchTab('list')
})

/* ---------- 7. 返回 ---------- */
const goBack = () => router.push('/')
/* ---------- 8. Monaco 预加载 ---------- */
const loadStatus = ref('loading')
const editorReady = computed(() => loadStatus.value === 'success')
const isStatusVisible = ref(true)

async function preloadMonaco() {
  try {
    await import('monaco-editor')
    await import('monaco-editor/min/vs/editor/editor.main.css')
    loadStatus.value = 'success'
  } catch {
    loadStatus.value = 'error'
  }
}

function hideStatus() {
  isStatusVisible.value = false
}

/* ---------- 9. 空闲预拉取其它 chunk ---------- */
function prefetchIdle() {
  const doPrefetch = () => {
    AsyncContest.__asyncLoader && AsyncContest.__asyncLoader()
    AsyncUser.__asyncLoader && AsyncUser.__asyncLoader()
    AsyncRank.__asyncLoader && AsyncRank.__asyncLoader()
    AsyncProfile.__asyncLoader && AsyncProfile.__asyncLoader()
  }
  if (typeof window !== 'undefined' && 'requestIdleCallback' in window) {
    window.requestIdleCallback(doPrefetch, { timeout: 2000 })
  } else {
    setTimeout(doPrefetch, 800)
  }
}

/* ---------- 10. 挂载 ---------- */
onMounted(async () => {
  preloadMonaco()
  await nextTick()
  if (!route.query.tab) {
    router.replace({ path: route.path, query: { ...route.query, tab: activeTab.value } })
  }
  prefetchIdle()
})
</script>

<style scoped>
.back-btn {
  background: #ef4444;
  color: #fff;
  padding: 8px 14px;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.3s ease;
}
.back-btn:hover {
  background: #dc2626;
}
.contest-parent {
  height: 100vh;
  display: flex;
  flex-direction: column;
  font-family: system-ui, -apple-system, BlinkMacSystemFont, sans-serif;
}
.title-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  background: #f5f5f5;
  border-bottom: 1px solid #d1d5e0;
  position: relative;
  min-height: 56px;
}
.title-item {
  padding: 10px 18px;
  cursor: pointer;
  font-weight: 600;
  border-radius: 6px;
  transition: background 0.2s, color 0.2s;
  user-select: none;
}
.title-item:hover {
  background: #e8eefc;
}
.title-item.active {
  background: #3b82f6;
  color: white;
}
.spacer {
  flex: 1;
}
.nav-status {
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  z-index: 20;
  padding: 8px 16px;
  border-radius: 14px;
  min-width: 340px;
  max-width: calc(100% - 160px);
  text-align: center;
  font-weight: 700;
  font-size: 14px;
  box-shadow: 0 6px 18px rgba(16, 24, 40, 0.08);
  color: #ffffff;
  cursor: default;
  user-select: none;
  transition: background-color 0.25s ease, transform 0.12s ease;
}
.nav-status[title] {
  cursor: pointer;
}
.status-loading {
  background-color: #d1a500;
}
.status-success {
  background-color: #16a34a;
}
.status-error {
  background-color: #dc2626;
}
.nav-status:active {
  transform: translate(-50%, -48%);
}
.content {
  content-visibility: auto;
  contain-intrinsic-size: 800px;
  flex: 1;
  position: relative;
  padding: 0;
}
.skeleton {
  padding: 24px;
}
.skeleton-line {
  height: 14px;
  border-radius: 7px;
  background: linear-gradient(90deg, #eee, #f5f5f5, #eee);
  background-size: 200% 100%;
  animation: shine 1.2s infinite linear;
  margin: 10px 0;
}
.skeleton-line.w-60 { width: 60%; }
.skeleton-line.w-75 { width: 75%; }
.skeleton-line.w-80 { width: 80%; }
.skeleton-line.w-85 { width: 85%; }
.skeleton-line.w-90 { width: 90%; }
.tab-pane {
  height: 100%;
  width: 100%;
  overflow: auto;
}
</style>
