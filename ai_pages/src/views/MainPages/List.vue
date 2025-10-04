<template>
  <div class="problem-list-container">
    <h2>题单列表</h2>

    <form @submit.prevent="handleSearch">
      <input
        ref="searchInput"
        v-model="searchKeyword"
        placeholder="搜索题目..."
        class="search-input"
        @keyup.enter="handleSearch"
        @blur="handleSearch"
      >
    </form>

    <!-- 🔧 工具栏：左中右三段 -->
    <div class="toolbar">
      <!-- 左：排序 + 更新 -->
      <div class="toolbar-left"><!-- 替换原来的下拉结构 -->
        <div ref="sortDrop" class="dropdown">
          <button class="btn" @click="showSortOptions = !showSortOptions">
            排序 <span class="caret">▾</span>
          </button>

          <!-- 原下拉菜单，保持不变 -->
          <div v-if="showSortOptions" class="dropdown-menu" @mousedown.stop>
            <label class="dropdown-item">
              <input type="radio" value="name" v-model="sortField" @change="fetchProblems" />
              按英文名
            </label>
            <label class="dropdown-item">
              <input type="radio" value="cnname" v-model="sortField" @change="fetchProblems" />
              按中文名
            </label>
            <label class="dropdown-item">
              <input type="radio" value="difficulty" v-model="sortField" @change="fetchProblems" />
              按难度
            </label>
          </div>
        </div>
      </div>

      <!-- 中：分页 -->
      <div class="toolbar-center">
        <button class="btn" @click="prevPage" :disabled="currentPage === 1">上一页</button>
        <span class="page-indicator">第 {{ currentPage }} 页</span>
        <button class="btn" @click="nextPage" :disabled="!hasMore">下一页</button>
      </div>

      <!-- 右：创建/历史 -->
      <div class="toolbar-right">
        <button
          v-if="userInfo && (userInfo === 'MANAGER' || userInfo === 'ROOT')"
          class="btn"
          @click="navigateToCreateProblem"
        >
          创建题目
        </button>
        <button class="btn" @click="navigateToHistory">历史记录</button>
      </div>
    </div>

    <!-- 列表渲染（占据剩余高度；内部滚动） -->
    <ul class="problem-list">
      <li v-for="item in displayedProblems" :key="item.name" class="problem-item" :class="{ done: item.accepted }">
        <div class="problem-item-content">
          <router-link :to="`/editor/${item.name}`" class="problem-link">
            <div class="title-line">
              <strong>{{ item.cnname || '(无中文名)' }}</strong>

              <span class="tag" v-if="item.tags">{{ item.tags }}</span>
              <span class="difficulty" :style="{ background: difficultyColor(item.difficulty) }">
                {{ item.difficulty }}
              </span>
            </div>
            <div class="meta-line">
              {{ item.name }}
            </div>
          </router-link>

          <div v-if="userInfo && (userInfo === 'MANAGER' || userInfo === 'ROOT')" class="modify-link">
            <router-link :to="`/form?name=${item.name}`">修改</router-link>
          </div>
        </div>
      </li>
    </ul>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, getCurrentInstance, watch } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const instance = getCurrentInstance()
const ip = instance?.appContext.config.globalProperties.$ip || 'localhost:7777'

/** ------------ 响应式状态 ------------ **/
const allProblems = ref([])
const displayedProblems = computed(() => allProblems.value)

const searchKeyword = ref('')
const lastKeyword   = ref('')
const sortField = ref('difficulty')
const showSortOptions = ref(false)

const currentPage = ref(1)
const pageSize = 10
const hasMore = ref(true)
const userInfo = ref(null)

const order = ref('')
const like = ref('')

/** ------------ 工具函数 ------------ **/
const parseJwt = (token) => {
  try {
    const base64Url = token.split('.')[1]
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/')
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split('')
        .map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    )
    return JSON.parse(jsonPayload)
  } catch {
    return {}
  }
}

const loadUserInfo = () => {
  const token = localStorage.getItem('jwt')
  if (!token) {
    alert('未登录或 JWT 错误')
    return
  }
  const parsed = parseJwt(token)
  userInfo.value = parsed.role ?? null
}

/** ------------ 拉取列表 ------------ **/
const fetchProblems = async () => {
  try {
    const token = localStorage.getItem('jwt') || ''
    const useSearchApi = !!(like.value || order.value)

    const resolvedOrder =
      sortField.value === 'difficulty' ? 'difficulty'
      : sortField.value === 'cnname'   ? 'cnname'
      : /* 默认 */                     'name'

    const params = new URLSearchParams({
      start: String((currentPage.value - 1) * pageSize),
      limit: String(pageSize),
      order: resolvedOrder,
      like: searchKeyword.value.trim()
    })

    const url = useSearchApi
      ? `http://${ip}/practice/searchList?${params}`
      : `http://${ip}/practice/list?${params}`

    const res = await fetch(url, {
      method: 'GET',
      headers: { Authorization: `Bearer ${token}` }
    })

    const json = await res.json()
    if (json.code === "0" && Array.isArray(json.data.list)) {
      allProblems.value = json.data.list
      hasMore.value = json.data.amount > currentPage.value * pageSize
    } else {
      allProblems.value = []
      hasMore.value = false
    }
  } catch (e) {
    allProblems.value = []
    hasMore.value = false
    alert('无法连接到服务器')
  }
}

// script setup 里加一个纯函数
function difficultyColor(val) {
  // 把 0-100 映射到 0-1
  const ratio = Math.min(1, Math.max(0, val / 100))
  // HSL：0°=红，120°=绿，越难越红
  const hue = 120 * (1 - ratio)
  return `hsl(${hue}, 80%, 45%)`
}

const sortDrop = ref(null)
const searchInput = ref(null)


function clickOutside(e) {
  if (showSortOptions.value && !sortDrop.value?.contains(e.target)) {
    showSortOptions.value = false
  }
}

/** ------------ 搜索 / 排序 / 分页 ------------ **/

const handleSearch = () => {
  const kw = searchKeyword.value.trim()
  if (kw === lastKeyword.value) return
  lastKeyword.value = kw
  currentPage.value = 1
  like.value = kw
  fetchProblems()
}

const prevPage = () => {
  if (currentPage.value > 1) {
    currentPage.value--
    fetchProblems()
  }
}

const nextPage = () => {
  if (hasMore.value) {
    currentPage.value++
    fetchProblems()
  }
}

watch(sortField, () => {
  currentPage.value = 1
  fetchProblems()
})

/** ------------ 路由 ------------ **/
const navigateToCreateProblem = () => router.push('/form')
const navigateToHistory = () => router.push('/history')

/** ------------ 启动 ------------ **/
onMounted(() => {
  document.addEventListener('mousedown', clickOutside)
  loadUserInfo()
  fetchProblems()
})

</script>

<style scoped>
/* ===================== 重新设计的布局核心 ===================== */
/* 用 Grid 明确分四行：标题、搜索、工具栏、列表（可滚动） */
.problem-list-container {
  /* —— 视口高度：兼容各浏览器 —— */
  height: 100vh;                 /* 基础 */
}
@supports (height: 100svh) {
  .problem-list-container { height: 100svh; }  /* 小视口单位（解决移动端地址栏收起/展开） */
}
@supports (height: 100dvh) {
  .problem-list-container { height: 100dvh; }  /* 动态视口单位（iOS 15+/现代浏览器） */
}

.problem-list-container {
  /* 安全区与底部缓冲 */
  --safe-bottom: max(16px, env(safe-area-inset-bottom));
  --list-bottom-gap: clamp(64px, 10vh, 128px);

  max-width: 900px;
  margin: 0 auto;
  padding: 16px 24px 0;          /* 顶部/左右留白，底部不留，避免双重内边距 */
  padding-bottom: var(--safe-bottom);

  background: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  font-family: 'Helvetica Neue', Arial, sans-serif;

  /* 用 Grid 精准分配高度 */
  display: grid;
  grid-template-rows: auto auto auto 1fr;  /* h2 / 搜索 / 工具栏 / 列表 */
  gap: 12px;

  /* 仅让最后一行（列表）滚动，外层不裁切下边界的安全区 */
  overflow: hidden;
}

/* ===================== 标题与搜索 ===================== */
h2 {
  margin: 0;
}
.search-input {
  width: 100%;
  padding: 10px 14px;
  font-size: 16px;
  border: 1px solid #ddd;
  border-radius: 8px;
  outline: none;
  transition: 0.3s;
}
.search-input:focus {
  border-color: #2563eb;
  box-shadow: 0 0 6px rgba(37, 99, 235, 0.2);
}

/* ===================== 工具栏 ===================== */
.toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
}
.toolbar-left, .toolbar-right {
  display: flex;
  align-items: center;
  gap: 10px;
}
.toolbar-center {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-left: auto;
  margin-right: auto;
}
.page-indicator {
  color: #374151;
  font-size: 14px;
}

/* 按钮 */
.btn {
  padding: 8px 16px;
  background-color: #2563eb;
  color: #fff;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  transition: background 0.25s, transform 0.05s;
  white-space: nowrap;
}
.btn:hover { background-color: #1e4db7; }
.btn:active { transform: translateY(1px); }
.btn:disabled { background: #9ca3af; cursor: not-allowed; }

/* 下拉 */
.dropdown { position: relative; }
.caret { margin-left: 6px; font-size: 12px; opacity: 0.9; }
.dropdown-menu {
  position: absolute;
  top: 42px;
  left: 0;
  min-width: 180px;
  background: #fff;
  border: 1px solid #ddd;
  border-radius: 8px;
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.08);
  padding: 10px 12px;
  z-index: 10;
}
.dropdown-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 0;
  font-size: 14px;
  cursor: pointer;
}

/* ===================== 列表（滚动区，关键重构） ===================== */
.problem-list {
  /* 作为 Grid 最后一行的可滚动区域 */
  list-style: none;
  margin: 0;
  padding: 0;

  /* ✅ 关键：允许在 Grid/Flex 容器内正确收缩并产生滚动 */
  min-height: 0;               /* 解决“滚不动到最底部”的常见根因 */
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
  overscroll-behavior: contain;

  /* ✅ 始终预留可见的底部缓冲 + iOS 安全区，避免最后几项被遮挡 */
  padding-bottom: 0;           /* 真正的缓冲通过 ::after 提供，避免被最后一项 margin 抵消 */
}
.problem-list::after {
  content: "";
  display: block;
  height: calc(var(--list-bottom-gap) + var(--safe-bottom));
}

/* 可选：顶部/底部渐隐，提示可滚动（纯视觉，不影响交互） */
/*
.problem-list {
  mask-image: linear-gradient(to bottom, transparent 0, black 16px, black calc(100% - 16px), transparent 100%);
}-  text-align: center;
*/

/* 列表项 */
/* 样式 */
.difficulty {
  display: inline-block;
  margin-left: 6px;
  padding: 2px 6px;
  border-radius: 4px;
  color: #fff;
  font-size: 12px;
  font-weight: 600;
  min-width: 20px;
  text-align: center;
  /* 不要写 background ！ */
}


.problem-item {
  background: #f9fafb;
  margin-bottom: 12px;
  padding: 14px;
  border-radius: 8px;
  transition: box-shadow 0.3s, transform 0.3s;
}
.problem-item:last-child { margin-bottom: 0; }
.problem-item:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  transform: translateY(-2px);
}
.problem-item-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.problem-link { text-decoration: none; color: inherit; }
.problem-item strong { font-size: 18px; color: #111827; }
.meta-line { font-size: 14px; color: #6b7280; }

.modify-link a {
  color: #2563eb;
  font-size: 14px;
  text-decoration: none;
}
.modify-link a:hover { text-decoration: underline; }/* 放在 <style scoped> 最后 */
.problem-item.done {
  background: #d1fae5; /*  Tailwind green-100  */
}
/* 标签 */
.tag {
  font-size: 12px;
  padding: 2px 6px;
  border-radius: 4px;
  background: #e5e7eb;   /* 灰色 */
  color: #374151;        /* 深灰文字 */
  white-space: nowrap;
}

</style>

