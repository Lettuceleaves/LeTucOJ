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

    <!-- 根据用户身份显示不同的按钮 -->
    <div class="action-buttons">
      <!-- 如果用户是 MANAGER 或 ROOT -->
      <div v-if="userInfo && (userInfo === 'MANAGER' || userInfo === 'ROOT')">
        <button @click="navigateToCreateProblem">创建题目</button>
        <button @click="navigateToManageUsers">用户管理</button>
      </div>

      <!-- 所有用户都能访问的按钮 -->
      <div>
        <button @click="navigateToHistory">历史记录</button>
        <button @click="navigateToCompetition">比赛</button>
      </div>
    </div>

    <!-- 题目列表 -->
    <ul class="problem-list">
      <li v-for="item in displayedProblems" :key="item.name" class="problem-item">
        <div class="problem-item-content">
          <router-link :to="`/editor/${item.name}`">
          <div><strong>{{ item.cnname || '(无中文名)' }}</strong></div>
          <div style="font-size: 0.9em; color: gray;">
              [英文名: {{ item.name }}] · {{ item.caseAmount }} 个测试点
          </div>
          </router-link>

          <!-- 如果是USER或ROOT用户，显示“修改”按钮 -->
          <div v-if="userInfo && (userInfo === 'USER' || userInfo === 'ROOT')" class="modify-link">
            <router-link :to="`/form?name=${item.name}`">修改</router-link>
          </div>
        </div>
      </li>
    </ul>

    <!-- 分页 -->
    <div class="pagination">
      <button @click="prevPage" :disabled="currentPage === 1">上一页</button>
      <span>第 {{ currentPage }} 页</span>
      <button @click="nextPage" :disabled="!hasMore">下一页</button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, getCurrentInstance } from 'vue';
import { useRouter } from 'vue-router';

const allProblems = ref([]); // 所有题目数据
const searchKeyword = ref('');
const sortField = ref('');
const showSortOptions = ref(false);
const router = useRouter();

const currentPage = ref(1);
const pageSize = 10;
const hasMore = ref(true); // 是否还有更多数据
const instance = getCurrentInstance()
const ip = instance.appContext.config.globalProperties.$ip

// 用户身份信息
const userInfo = ref(null);

// 解析 JWT
const parseJwt = (token) => {
  const base64Url = token.split('.')[1];  // 获取 JWT 中的 payload 部分
  const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');  // 修正 base64 编码
  const jsonPayload = decodeURIComponent(atob(base64).split('').map(function(c) {
    return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
  }).join(''));

  return JSON.parse(jsonPayload);  // 返回解析后的 payload
};

// 获取 JWT 并解析
const loadUserInfo = () => {
  const token = localStorage.getItem('jwt');
  if (token) {
    const parsedData = parseJwt(token);
    userInfo.value = parsedData.role;  // 存储用户信息
  } else {
    alert('JWT错误或未登录');
  }
};

// 拉取所有题目信息
const fetchProblems = async () => {
  try {
    const token = localStorage.getItem('jwt')
    const response = await fetch(`http://${ip}:7777/practice/basicinfo`, {
      method: 'POST',
      headers: { 
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`  // 加上这行
      },
      body: JSON.stringify({
        type: 'SELECT',
        subType: 'LIST',
        name: null,
        cnname: null,
        start: (currentPage.value - 1) * pageSize,
        limit: pageSize,
        data: searchKeyword.value ? { name: searchKeyword.value } : {}
      }),
    });

    const result = await response.json();

    if (result.type === 1 && Array.isArray(result.data)) {
      allProblems.value = result.data;
      hasMore.value = result.data.length === pageSize; // 判断是否满页
    } else {
      alert('获取题目列表失败：' + result.message);
      hasMore.value = false;
    }
  } catch (error) {
    alert('网络错误：' + error.message);
    hasMore.value = false;
  }
};


// 筛选并排序
const filteredProblems = computed(() => {
  let result = allProblems.value;

  if (searchKeyword.value.trim()) {
    const keyword = searchKeyword.value.trim().toLowerCase();
    result = result.filter(p => p.name?.toLowerCase().includes(keyword));
  }

  if (sortField.value === 'name') {
    result = [...result].sort((a, b) => a.name.localeCompare(b.name));
  } else if (sortField.value === 'caseAmount') {
    result = [...result].sort((a, b) => b.caseAmount - a.caseAmount);
  }

  return result;
});

const displayedProblems = computed(() => {
  let result = allProblems.value;

  // 本页数据已是分页后数据，无需 slice
  if (searchKeyword.value.trim()) {
    const keyword = searchKeyword.value.trim().toLowerCase();
    result = result.filter(p => p.name?.toLowerCase().includes(keyword));
  }

  if (sortField.value === 'name') {
    result = [...result].sort((a, b) => a.name.localeCompare(b.name));
  } else if (sortField.value === 'caseAmount') {
    result = [...result].sort((a, b) => b.caseAmount - a.caseAmount);
  }

  return result;
});

const prevPage = () => {
  if (currentPage.value > 1) {
    currentPage.value--;
    fetchProblems(); // 🔁 重新获取上一页数据
  }
};

const nextPage = () => {
  if (hasMore.value) {
    currentPage.value++;
    fetchProblems(); // 🔁 重新获取下一页数据
  }
};

const handleSearch = () => {
  currentPage.value = 1;
  fetchProblems();
};

// 跳转到创建题目页面
const navigateToCreateProblem = () => {
  router.push('/form');
};

// 跳转到用户管理页面
const navigateToManageUsers = () => {
  router.push('/manage-users');
};

// 跳转到历史记录页面
const navigateToHistory = () => {
  router.push('/history');
};

// 跳转到比赛页面
const navigateToCompetition = () => {
  router.push('/competition');
};

onMounted(() => {
  loadUserInfo();  // 在页面加载时解析 JWT 并获取用户信息
  fetchProblems();
});

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
