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
        :class="{ active: activeTab === 'user' }"
        @click="switchTab('user')"
      >用户</div>

      <div class="spacer"></div>

      <!-- ✅ 新增返回按钮 -->
      <button class="back-btn" @click="goBack">返回</button>
    </div>

    <!-- 内容区域：Suspense 外层，KeepAlive 放到 default 槽；动态组件加 key -->
    <div class="content">
      <Suspense>
        <template #default>
          <KeepAlive>
            <component
              :is="currentComp"
              :key="activeTab"
              class="tab-pane"
            />
          </KeepAlive>
        </template>

        <!-- 骨架屏 -->
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

// --- 读取 URL 上的 ?tab= 值，默认 'list'
const initialTab = (typeof route.query.tab === 'string' ? route.query.tab : 'list')
const activeTab = ref(['list', 'contest', 'user'].includes(initialTab) ? initialTab : 'list')

// --- 异步组件（首屏仅加载题单；其他在空闲时预拉取） ---
const AsyncProblemList = defineAsyncComponent(() =>
  import(/* webpackChunkName: "tab-problem-list" */ './MainPages/List.vue')
)
const AsyncContest = defineAsyncComponent(() =>
  import(/* webpackChunkName: "tab-contest" */ './MainPages/Contest.vue')
)
const AsyncUser = defineAsyncComponent(() =>
  import(/* webpackChunkName: "tab-user" */ '../views/UserPage.vue')
)

// 当前激活组件映射
const compMap = {
  list: AsyncProblemList,
  contest: AsyncContest,
  user: AsyncUser
}
const currentComp = computed(() => compMap[activeTab.value])

const goBack = () => {
  router.back()  // 或 router.go(-1)
}

// 切换标签：顺带把 tab 写回 URL（不会刷新页面）
function switchTab(tab) {
  if (tab === activeTab.value) return
  activeTab.value = tab
}

// 同步：当 activeTab 改变时，把 ?tab=xxx 写入 URL
watch(activeTab, (val) => {
  const q = { ...route.query, tab: val }
  router.replace({ path: route.path, query: q })
})

// 同步：当 URL 上的 tab 变化时（外部跳转/用户改地址），更新 activeTab
watch(() => route.query.tab, (val) => {
  if (typeof val === 'string' && ['list', 'contest', 'user'].includes(val)) {
    if (val !== activeTab.value) activeTab.value = val
  }
})

// 空闲时预取：首屏稳定后预拉取其它两个标签组件的 chunk
function prefetchIdle() {
  const doPrefetch = () => {
    // 触发动态 import（浏览器会从缓存中命中）
    AsyncContest.__asyncLoader && AsyncContest.__asyncLoader()
    AsyncUser.__asyncLoader && AsyncUser.__asyncLoader()
  }
  if (typeof window !== 'undefined' && 'requestIdleCallback' in window) {
    window.requestIdleCallback(doPrefetch, { timeout: 2000 })
  } else {
    setTimeout(doPrefetch, 800)
  }
}

onMounted(async () => {
  await nextTick()
  // 若首次进入没有 tab，则把默认 tab 写回 URL，保证后续返回可还原
  if (!route.query.tab) {
    router.replace({ path: route.path, query: { ...route.query, tab: activeTab.value } })
  }
  prefetchIdle()
})

/** --- 可选的页面跳转函数：保留你原有逻辑（若外部仍在使用） --- **/
const onCreate = () => router.push('/form')
const onUser = () => router.push('/user')
const onHistory = () => router.push('/history')
const onCompetition = () => router.push('/competition')
</script>


<style scoped>

.back-btn {
  background: #ef4444; /* 红色按钮，易识别 */
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
  background: #dc2626; /* hover 深红色 */
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

.content {
  /* 关键：避免首屏绘制整块内容，按需渲染可见后代 */
  content-visibility: auto;
  contain-intrinsic-size: 800px; /* 估算高度，防 layout shift，可按需调整 */

  flex: 1;
  position: relative;
  overflow: hidden; /* 首页不滚，由各自子页内部分页控制 */
  padding: 0;
}

/* 骨架屏样式 */
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

@keyframes shine {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

/* 可选：给每个 tab pane 一个容器类 */
.tab-pane {
  height: 100%;
  width: 100%;
  overflow: hidden;
}
</style>
