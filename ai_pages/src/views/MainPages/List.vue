<template>
  <div class="problem-list-container">
    <h2>题单列表</h2>

    <!-- 搜索框 -->
    <input
      v-model="searchKeyword"
      @input="handleSearch"
      placeholder="搜索题目..."
      class="search-input"
    />

    <!-- 排序按钮与选项 -->
    <div class="sort-container">
      <button @click="showSortOptions = !showSortOptions">排序</button>
      <div v-if="showSortOptions" class="sort-options">
        <label>
          <input type="radio" value="name" v-model="sortField" @change="fetchProblems" />
          按题目名称
        </label>
        <label>
          <input type="radio" value="caseAmount" v-model="sortField" @change="fetchProblems" />
          按测试用例数量
        </label>
      </div>
    </div>

    <!-- 权限控制按钮 -->
    <div class="action-buttons">
      <div v-if="userInfo && (userInfo === 'MANAGER' || userInfo === 'ROOT')">
        <button @click="navigateToCreateProblem">创建题目</button>
        <button @click="navigateToManageUsers">用户管理</button>
      </div>

      <div>
        <button @click="navigateToHistory">历史记录</button>
        <button @click="navigateToCompetition">比赛</button>
      </div>
    </div>

    <!-- 列表渲染 -->
    <ul class="problem-list">
      <li v-for="item in displayedProblems" :key="item.name" class="problem-item">
        <div class="problem-item-content">
          <router-link :to="`/editor/${item.name}`">
            <div>
              <strong>{{ item.cnname || '(无中文名)' }}</strong>
            </div>
            <div style="font-size: 0.9em; color: gray">
              [英文名: {{ item.name }}] · {{ item.caseAmount }} 个测试点
            </div>
          </router-link>

          <div v-if="userInfo && (userInfo === 'USER' || userInfo === 'ROOT')" class="modify-link">
            <router-link :to="`/form?name=${item.name}`">修改</router-link>
          </div>
        </div>
      </li>
    </ul>

    <!-- 分页控制 -->
    <div class="pagination">
      <button @click="prevPage" :disabled="currentPage === 1">上一页</button>
      <span>第 {{ currentPage }} 页</span>
      <button @click="nextPage" :disabled="!hasMore">下一页</button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, getCurrentInstance } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const instance = getCurrentInstance()

const REMOTE_SERVER_URL = import.meta.env.VITE_REMOTE_SERVER_URL

const allProblems = ref([])
const searchKeyword = ref('')
const sortField = ref('')
const showSortOptions = ref(false)

const currentPage = ref(1)
const pageSize = 10
const hasMore = ref(true)
const userInfo = ref(null)

// JWT解析
const parseJwt = (token) => {
  const base64Url = token.split('.')[1]
  const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/')
  const jsonPayload = decodeURIComponent(
    atob(base64)
      .split('')
      .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
      .join(''),
  )
  return JSON.parse(jsonPayload)
}

const loadUserInfo = () => {
  const token = localStorage.getItem('jwt')
  if (token) {
    const parsed = parseJwt(token)
    userInfo.value = parsed.role
  } else {
    alert('未登录或 JWT 错误')
  }
}

const fetchProblems = async () => {
  try {
    const token = localStorage.getItem('jwt')
    // 拼 query 参数
    const params = new URLSearchParams({
      start: (currentPage.value - 1) * pageSize,
      limit: pageSize,
      order: sortField.value === 'caseAmount' ? 'caseAmount' : 'name', // 按你的单选按钮
      like: searchKeyword.value.trim() || '',
    })

    const res = await fetch(`${REMOTE_SERVER_URL}/practice/list?${params}`, {
      method: 'GET',
      headers: { Authorization: `Bearer ${token}` },
    })

    const json = await res.json()
    if (json.status === 0 && Array.isArray(json.data)) {
      // 假设后端直接返回数组
      allProblems.value = json.data
      hasMore.value = json.data.length === pageSize
    } else {
      alert(json.message || '加载失败')
      hasMore.value = false
    }
  } catch (e) {
    alert('网络错误：' + e.message)
    hasMore.value = false
  }
}

// 排序和筛选
const displayedProblems = computed(() => {
  let data = allProblems.value

  if (searchKeyword.value.trim()) {
    const keyword = searchKeyword.value.trim().toLowerCase()
    data = data.filter((p) => p.name?.toLowerCase().includes(keyword))
  }

  if (sortField.value === 'name') {
    data = [...data].sort((a, b) => a.name.localeCompare(b.name))
  } else if (sortField.value === 'caseAmount') {
    data = [...data].sort((a, b) => b.caseAmount - a.caseAmount)
  }

  return data
})

const handleSearch = () => {
  currentPage.value = 1
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

// 跳转逻辑
const navigateToCreateProblem = () => router.push('/form')
const navigateToManageUsers = () => router.push('/manage-users')
const navigateToHistory = () => router.push('/history')
const navigateToCompetition = () => router.push('/competition')

onMounted(() => {
  loadUserInfo()
  fetchProblems()
})
</script>

<style scoped>
.problem-list-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}
.search-input {
  width: 100%;
  padding: 8px;
  margin-bottom: 12px;
  border: 1px solid #ccc;
  border-radius: 4px;
}
.sort-container {
  margin-bottom: 10px;
  position: relative;
}
.sort-container button {
  padding: 6px 12px;
  background-color: #3b82f6;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}
.sort-options {
  background: #fff;
  border: 1px solid #ddd;
  padding: 8px;
  position: absolute;
  top: 40px;
  left: 0;
  z-index: 10;
  border-radius: 6px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}
.problem-list {
  list-style: none;
  padding: 0;
}
.problem-item {
  padding: 10px;
  border-bottom: 1px solid #eee;
}
.problem-item-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.pagination {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 20px;
}
</style>
