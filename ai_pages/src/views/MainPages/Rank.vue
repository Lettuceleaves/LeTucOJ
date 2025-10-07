<template>
  <div class="rank-page">
    <h2 class="title">用户排行榜</h2>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading">加载中...</div>

    <!-- 错误状态 -->
    <div v-else-if="error" class="error">{{ error }}</div>

    <!-- 表格 -->
    <table v-else class="rank-table">
      <thead>
        <tr>
          <th>排名</th>
          <th>姓名</th>
          <th>用户名</th>
          <th>题数</th>
          <th>总分</th>
        </tr>
      </thead>
      <tbody>
        <tr
          v-for="(item, idx) in sortedData"
          :key="item.userName"
          :class="[medalClass(idx), 'clickable-row']"
          @click="goToProfile(item.userName)"         >
          <td class="rank-number">{{ idx + 1 }}</td>
          <td>{{ item.cnname }}</td>
          <td>{{ item.userName }}</td>
          <td>{{ item.count }}</td>
          <td class="score">{{ item.totalScore }}</td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, getCurrentInstance } from 'vue'
import { useRouter } from 'vue-router'

/* ---------- 基础响应式数据 ---------- */
const instance = getCurrentInstance()
const ip = instance?.appContext.config.globalProperties.$ip

const rankData = ref([])   // 原始数据
const loading  = ref(true)
const error    = ref(null)

const router = useRouter()

/* ---------- 跳转到用户详情页 ---------- */
function goToProfile(userName) {
    const route = router.resolve({ 
        name: 'othersProfile', 
        query: { 
            pname: userName 
        } 
    });
    window.open(route.href, '_blank');
}

/* ---------- 排序：总分降序 -> 题数降序 -> 用户名升序 ---------- */
const sortedData = computed(() =>
  [...rankData.value].sort((a, b) => {
    if (b.totalScore !== a.totalScore) return b.totalScore - a.totalScore
    if (b.count !== a.count) return b.count - a.count
    return a.userName.localeCompare(b.userName)
  })
)

/* ---------- 前三名样式 ---------- */
const medalClass = idx =>
  idx === 0 ? 'first' : idx === 1 ? 'second' : idx === 2 ? 'third' : ''

/* ---------- 获取数据 ---------- */
async function fetchRankData() {
  try {
    const token = localStorage.getItem('jwt');
    const res = await fetch(`http://${ip}/user/rank`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    });
    const { code, data, error } = await res.json(); // 一次性解构

    if (code === '0') {
      rankData.value = data || [];
    } else {
      throw new Error(error || '获取数据失败');
    }
  } catch (err) {
    error.value = err.message || '网络错误';
  } finally {
    loading.value = false;
  }

}

onMounted(() => {
  fetchRankData()
})
</script>

<style scoped>
.rank-page {
  padding: 20px;
  font-family: system-ui, -apple-system, sans-serif;
}

.title {
  font-size: 26px;
  font-weight: 700;
  margin-bottom: 20px;
  color: #1e293b;
}

.loading { color: #555; font-size: 16px; }
.error   { color: #dc2626; font-size: 16px; }

/* ---------- 表格 ---------- */
.rank-table {
  width: 100%;
  border-collapse: separate;
  border-spacing: 0;
  text-align: center;
  box-shadow: 0 4px 12px rgba(0,0,0,.05);
  border-radius: 10px;
  overflow: hidden;
}

.rank-table th {
  background: linear-gradient(90deg, #3b82f6, #60a5fa);
  color: #fff;
  font-weight: 600;
  padding: 12px 0;
  font-size: 16px;
}

.rank-table td {
  padding: 12px 0;
  font-size: 15px;
  color: #334155;
}

.rank-table tbody tr {
  background: #f9fafb;
  transition: background .2s;
}
.rank-table tbody tr:nth-child(even) { background: #ffffff; }
.rank-table tbody tr:hover { background: #e0f2fe; }

.rank-number { font-weight: 700; }
.score       { font-weight: 600; color: #059669; }

/* 前三名奖牌色 */
.first  .rank-number { color: #f59e0b; font-size: 18px; }
.second .rank-number { color: #94a3b8; font-size: 17px; }
.third  .rank-number { color: #f97316; font-size: 16px; }

/* ---------- 响应式 ---------- */
@media (max-width: 640px) {
  .rank-table th,
  .rank-table td { font-size: 14px; padding: 10px 0; }
  .title { font-size: 22px; }
}
</style>
