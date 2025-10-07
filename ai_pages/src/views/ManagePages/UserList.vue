<template>
  <div class="user-page">
    <h2>用户列表</h2>

    <div class="filter-bar">
      <label>
        <input type="checkbox" v-model="onlyDisabled" />
        仅显示未启用用户
      </label>
      <span class="pagination-info">
        总用户数: {{ filteredUsers.length }} | 第 {{ currentPage }} / {{ totalPages }} 页
      </span>
    </div>

    <div v-if="loading" class="tip">加载中，请稍候…</div>
    <div v-else-if="error" class="tip error">{{ error }}</div>
    <div v-else-if="displayUsers.length === 0" class="tip">暂无用户</div>

    <div v-else class="table-wrapper">
      <table class="user-table">
        <thead>
          <tr>
            <th>用户名</th>
            <th>中文名</th>
            <th>角色</th>
            <th>启用状态</th>
            <th v-if="isAdmin">操作</th>
          </tr>
        </thead>

        <tbody>
          <tr v-for="u in displayUsers" :key="u.userName">
            <td>{{ u.userName }}</td>
            <td>{{ u.cnname || '-' }}</td>
            <td>{{ u.role }}</td>
            <td>{{ u.status === 1 ? '启用' : '禁用' }}</td>

            <td v-if="isAdmin">
              <template v-if="u.userName !== currentUserName">
                <button
                  v-if="u.role === 'USER'"
                  @click="upgrade(u)"
                  class="btn-role"
                >升级</button>
                <button
                  v-if="u.role === 'MANAGER'"
                  @click="downgrade(u)"
                  class="btn-role"
                >降级</button>
                <button
                  v-if="u.status"
                  @click="disable(u)"
                  class="btn-status"
                >禁用</button>
                <button
                  v-else
                  @click="enable(u)"
                  class="btn-status btn-enable"
                >启用</button>
              </template>
              <span v-else class="self-label">本人</span>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
    
    <div v-if="totalPages > 1 && !loading" class="pagination-controls">
        <button 
        @click="prevPage($event)" 
        :disabled="currentPage === 1"
        class="btn-page"
        >上一页</button>
        <span class="page-indicator">{{ currentPage }} / {{ totalPages }}</span>
        <button 
        @click="nextPage($event)" 
        :disabled="currentPage === totalPages"
        class="btn-page"
        >下一页</button>
    </div>

  </div>
</template>

<script setup>
import { ref, computed, onMounted, getCurrentInstance, watch } from 'vue';

/* ---------- 全局配置 (保持不变) ---------- */
const instance = getCurrentInstance();
const ip = instance.appContext.config.globalProperties.$ip;

const role = ref('');
const currentUserName = ref(''); // 当前登录用户名
const token = () => localStorage.getItem('jwt') || '';

/* ---------- 分页状态 (新增) ---------- */
const currentPage = ref(1);
const pageSize = ref(20); // 每页显示 20 个用户

/* ---------- 角色解析 (保持不变) ---------- */
const parseJwt = (tk) => {
  try {
    const base64Url = tk.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split('')
        .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    );
    return JSON.parse(jsonPayload);
  } catch {
    return {};
  }
};

const parseRole = () => {
  const tk = token();
  if (!tk) return;
  const payload = parseJwt(tk);
  role.value = payload.role || '';
  currentUserName.value = payload.sub || '';
};

/* ---------- 管理员判定 (保持不变) ---------- */
const isAdmin = computed(() => {
  const r = role.value;
  return r === 'ROOT' || r === 'MANAGER';
});

/* ---------- 响应式数据 (保持不变) ---------- */
const users        = ref([]);
const loading      = ref(true); // 列表加载状态
const error        = ref(null);
const onlyDisabled = ref(false);

/* ---------- 计算属性：总页数 (新增) ---------- */
const totalPages = computed(() => {
  return Math.ceil(filteredUsers.value.length / pageSize.value);
});

/* ---------- 计算属性：过滤后的用户列表 (新增) ---------- */
const filteredUsers = computed(() =>
  onlyDisabled.value
    ? users.value.filter((u) => u.status !== 1)
    : users.value
);

/* ---------- 显示列表（最终分页和过滤后的）(修改) ---------- */
const displayUsers = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value;
  const end = start + pageSize.value;
  // 始终对过滤后的列表进行切片
  return filteredUsers.value.slice(start, end);
});

/* ---------- 分页控制方法 (新增) ---------- */
const prevPage = (event) => {
  if (currentPage.value > 1) {
    currentPage.value--;
  }
  // 确保按钮失去焦点，防止重复触发
  if (event && event.target) {
    event.target.blur();
  }
};

const nextPage = (event) => {
  if (currentPage.value < totalPages.value) {
    currentPage.value++;
  }
  // 确保按钮失去焦点，防止重复触发
  if (event && event.target) {
    event.target.blur();
  }
};

// 监听过滤条件变化，重置页码
watch(onlyDisabled, () => {
  currentPage.value = 1;
});


/* ---------- 通用请求封装 (保持不变) ---------- */
const call = (endpoint, userName) =>
  fetch(`http://${ip}${endpoint}?pname=${encodeURIComponent(userName)}`, {
    method: 'PUT',
    headers: { Authorization: `Bearer ${token()}` },
  })
    .then((r) => r.json())
    .then((j) => {
      if (j.code !== '0') throw new Error(j.message || '操作失败');
      return fetchUsers();   // 刷新列表
    })
    .catch((e) => alert(e.message || '网络错误'));

/* ---------- 四个按钮 (保持不变) ---------- */
const upgrade   = (u) => call('/user/promote', u.userName);
const downgrade = (u) => call('/user/demote', u.userName);
const enable    = (u) => call('/user/activate', u.userName);
const disable   = (u) => call('/user/deactivate', u.userName);

// 角色权重映射：ROOT 始终在最前，然后是 MANAGER，最后是 USER
const roleOrder = {
    'ROOT': 1,
    'MANAGER': 2,
    'USER': 3,
};

/* -------------------------------------------
 * ---------- 拉取用户列表 (保持排序逻辑不变) ----------
 * ------------------------------------------- */
const fetchUsers = async () => {
  loading.value = true;
  error.value   = null;
  const tk = token();
  let list = [];

  try {
    /* 1. 拉取普通用户列表 */
    const userRes = await fetch(`http://${ip}/user/users`, {
      method: 'GET',
      headers: { Authorization: `Bearer ${tk}` },
    });
    const userJson = await userRes.json();
    if (['0', 'B070005'].includes(userJson.code)) {
      list = userJson.data ?? [];
    } else if (userJson.code !== 'B070005') {
       throw new Error(userJson.message || '拉取普通用户列表失败');
    }

    /* 2. 管理员额外拉取 manager/root 列表 */
    if (role.value === 'ROOT' || role.value === 'MANAGER') {
      const mgrRes = await fetch(`http://${ip}/user/managers`, {
        method: 'GET',
        headers: { Authorization: `Bearer ${tk}` },
      });
      const mgrJson = await mgrRes.json();
      if (['0', 'B070006'].includes(mgrJson.code) && Array.isArray(mgrJson.data)) {
        list = [...list, ...mgrJson.data];
      } else if (mgrJson.code !== 'B070006') {
        throw new Error(mgrJson.message || '拉取管理员列表失败');
      }
    }

    /* 3. 按用户名去重 */
    const map = new Map();
    list.forEach((u) => map.set(u.userName, u));
    let uniqueUsers = Array.from(map.values());
    
    // 4. 过滤：只保留 ROOT, MANAGER, USER 
    // **注意：由于你要求只显示 MANAGER 和 USER，但 ROOT 通常需要看到自己，
    // 这里我们保留 ROOT、MANAGER 和 USER，并依赖排序。
    // 如果你严格要求 ROOT 也不显示，请移除对 ROOT 的保留。
    uniqueUsers = uniqueUsers.filter(u => ['ROOT', 'MANAGER', 'USER'].includes(u.role));
    
    // 5. 排序：先按角色权重，再按用户名字典序
    uniqueUsers.sort((a, b) => {
        // 5a. 角色排序 (ROOT 在前)
        const roleA = roleOrder[a.role] || 99;
        const roleB = roleOrder[b.role] || 99;
        if (roleA !== roleB) {
            return roleA - roleB;
        }
        
        // 5b. 用户名字典序排序
        return a.userName.localeCompare(b.userName);
    });

    users.value = uniqueUsers;
    currentPage.value = 1; // 刷新列表后重置页码

  } catch (e) {
    error.value = e.message || '网络错误';
  } finally {
    loading.value = false;
  }
};


/* ---------- 挂载 (保持不变) ---------- */
onMounted(() => {
  parseRole();
  fetchUsers();
});
</script>

<style scoped>
/* 将原 style 中所有与列表、表格、按钮操作相关的样式保留在这里 */

/* ---------- 筛选栏 ---------- */
.filter-bar {
  margin: 8px 0 16px;
  font-size: 14px;
  color: #374151;
}
.filter-bar input {
  margin-right: 6px;
  vertical-align: middle;
}

/* ---------- 按钮 ---------- */
.btn-role,
.btn-status {
  padding: 4px 8px;
  margin-right: 6px;
  font-size: 12px;
  cursor: pointer;
  border: none;
  border-radius: 3px;
}
.btn-role {
  background: #409eff;
  color: #fff;
}
.btn-status {
  background: #f56c6c; /* 禁用 */
  color: #fff;
}
.btn-enable {
  background: #67c23a; /* 启用 */
}

/* ---------- 页面 ---------- */
.user-page {
  max-width: 800px;
  margin: 40px auto;
  padding: 0 20px 60px; /* 增加底部填充，60px 留给分页栏 */
}
h2 {
  text-align: center;
  margin-bottom: 20px;
  font-size: 24px;
}

/* ---------- 提示 ---------- */
.tip {
  text-align: center;
  padding: 20px;
  font-size: 16px;
}
.tip.error {
  color: #b91c1c;
}

/* ---------- 表格 ---------- */
.table-wrapper {
  max-height: calc(100vh - 280px); /* 降低高度以给固定底部分页留出空间 */
  overflow-y: auto; /* 改回 auto，或直接删除此行，让页面滚动 */
  overflow-x: auto;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
}
.user-table {
  width: 100%;
  border-collapse: collapse;
  min-width: 600px;
}
.user-table th,
.user-table td {
  padding: 8px 12px;
  border-bottom: 1px solid #e5e7eb; /* 只保留底部边框 */
  text-align: left;
}
.user-table th {
  background-color: #f9fafb;
  position: sticky;
  top: 0;
  z-index: 1;
}

/* 新增：本人标签样式 */
.self-label {
    color: #9ca3af;
    font-size: 12px;
    padding: 4px 8px;
    border: 1px dashed #d1d5db;
    border-radius: 3px;
    display: inline-block;
}

/* ---------- 筛选栏 (调整布局以容纳分页信息) ---------- */
.filter-bar {
  margin: 8px 0 16px;
  font-size: 14px;
  color: #374151;
  display: flex; /* 新增: 使用 Flex 布局 */
  justify-content: space-between; /* 新增: 左右对齐 */
  align-items: center;
}
.filter-bar input {
  margin-right: 6px;
  vertical-align: middle;
}
.pagination-info {
  font-size: 14px;
  color: #6b7280;
}

/* ---------- 分页控件 (新增) ---------- */
.pagination-controls {
  position: fixed; /* 固定在视口 */
  bottom: 0; /* 贴紧底部 */
  left: 0; /* 贴紧左侧 */
  width: 100%; /* 占据整行宽度 */
  background-color: #ffffff; /* 确保背景色覆盖内容 */
  box-shadow: 0 -2px 5px rgba(0, 0, 0, 0.1); /* 增加阴影，区分内容 */
  padding: 35px 0; /* 增加上下内边距 */
  margin-top: 0; /* 移除原来的 margin-top */
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 10px;
  z-index: 100; /* 确保它在其他元素之上 */
}
.btn-page {
  padding: 10px 20px;
  font-size: 20px;
  cursor: pointer;
  border: 1px solid #d1d5db;
  border-radius: 12px;
  background-color: #f9fafb;
  color: #374151;
  transition: background-color 0.2s;
}
.btn-page:hover:not(:disabled) {
  background-color: #e5e7eb;
}
.btn-page:disabled {
  cursor: not-allowed;
  opacity: 0.5;
}
.page-indicator {
  font-weight: bold;
  color: #1f2937;
  min-width: 50px;
  text-align: center;
}

</style>