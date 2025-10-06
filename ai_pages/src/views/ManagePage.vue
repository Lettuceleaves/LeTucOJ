<template>
  <div class="user-page">
    <h2>用户列表</h2>

    <div class="filter-bar">
      <label>
        <input type="checkbox" v-model="onlyDisabled" />
        仅显示未启用用户
      </label>
    </div>

    <div class="refresh-container">
      <button
        @click="refreshSql"
        :class="['btn-refresh', { 'btn-refresh-success': refreshStatus === 'success' }]"
        :disabled="loading"
      >
        DB一键备份
      </button>
      <div v-if="showMessage" class="message-bubble" :style="{ opacity: messageOpacity }">
        {{ message }}
      </div>
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
                class="btn-status"
              >启用</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, getCurrentInstance } from 'vue';

/* ---------- 全局配置 ---------- */
const instance = getCurrentInstance();
const ip = instance.appContext.config.globalProperties.$ip;

const role = ref('');
const name = ref('');       // 当前登录用户名
const token = () => localStorage.getItem('jwt') || '';

/* ---------- 角色解析 ---------- */
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
  name.value = payload.sub || '';
};

/* ---------- 管理员判定 ---------- */
const isAdmin = computed(() => {
  const r = role.value;
  return r === 'ROOT' || r === 'MANAGER';
});

/* ---------- 响应式数据 ---------- */
const users        = ref([]);
const loading      = ref(true); // 列表加载状态
const error        = ref(null);
const onlyDisabled = ref(false);

// 新增响应式数据
const refreshStatus = ref('default');
const showMessage = ref(false);
const message = ref('');
const messageOpacity = ref(0);
const sqlLoading = ref(false); // **新增：SQL 刷新加载状态**

/* ---------- 显示列表（过滤后） ---------- */
const displayUsers = computed(() =>
  onlyDisabled.value
    ? users.value.filter((u) => !u.status)
    : users.value
);

/* ---------- 通用请求封装 ---------- */
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

/* ---------- 四个按钮 ---------- */
const upgrade   = (u) => call('/user/promote', u.userName);
const downgrade = (u) => call('/user/demote', u.userName);
const enable    = (u) => call('/user/activate', u.userName);
const disable   = (u) => call('/user/deactivate', u.userName);

/* ---------- 新增的刷新 SQL 方法 ---------- */
const refreshSql = async () => {
  sqlLoading.value = true; // **修改：使用新的加载状态**
  try {
    const tk = token();
    const res = await fetch(`http://${ip}/sys/refresh/sql`, {
      method: 'GET',
      headers: { Authorization: `Bearer ${tk}` },
    });
    const json = await res.json();

    if (json.code === '0') {
      refreshStatus.value = 'success';
      message.value = json.data || '缓存刷新成功';
      showMessage.value = true;
      messageOpacity.value = 1;

      setTimeout(() => {
        messageOpacity.value = 0;
        setTimeout(() => {
          showMessage.value = false;
          refreshStatus.value = 'default';
        }, 1000);
      }, 3000);

    } else {
      throw new Error(json.message || '刷新失败');
    }
  } catch (e) {
    alert(e.message);
    refreshStatus.value = 'default';
  } finally {
    sqlLoading.value = false; // **修改：使用新的加载状态**
  }
};


/* ---------- 拉取用户列表 ---------- */
const fetchUsers = async () => {
  loading.value = true;
  error.value   = null;
  const tk = token();

  try {
    /* 1. 普通用户列表（code 0 或 1 都算成功） */
    const res = await fetch(`http://${ip}/user/users`, {
      method: 'GET',
      headers: { Authorization: `Bearer ${tk}` },
    });
    const json = await res.json();
    if (!['0', 'B070005'].includes(json.code))
      throw new Error(json.message || '拉取用户列表失败');
    let list = json.data ?? [];

    /* 2. 管理员额外拉 manager 列表 */
    if (role.value === 'ROOT' || role.value === 'MANAGER') {
      const mgrRes = await fetch(`http://${ip}/user/managers`, {
        method: 'GET',
        headers: { Authorization: `Bearer ${tk}` },
      });
      const mgrJson = await mgrRes.json();
      if (['0', 'B070006'].includes(mgrJson.code) && Array.isArray(mgrJson.data)) {
        list = [...list, ...mgrJson.data];
      } else {
        throw new Error(mgrJson.message || '拉取 manager 列表失败');
      }
    }

    if (['B070005', 'B070006'].includes(json.code)) {
      throw new Error('当前无用户');
    }

    /* 3. 按用户名去重 */
    const map = new Map();
    list.forEach((u) => map.set(u.userName, u));
    users.value = Array.from(map.values());
  } catch (e) {
    error.value = e.message || '网络错误';
  } finally {
    loading.value = false;
  }
};


/* ---------- 挂载 ---------- */
onMounted(() => {
  parseRole();
  fetchUsers();
});
</script>

<style scoped>
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
  background: #f56c6c;
  color: #fff;
}

/* ---------- 页面 ---------- */
.user-page {
  max-width: 800px;
  margin: 40px auto;
  padding: 0 20px;
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
  max-height: calc(100vh - 220px);
  overflow-y: auto;
  overflow-x: auto;
  border: 0px solid #e5e7eb;
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
  border: 0px solid #e5e7eb;
  text-align: left;
}
.user-table th {
  background-color: #f9fafb;
  position: sticky;
  top: 0;
  z-index: 1;
}

/* --- 新增的样式 --- */
.refresh-container {
  position: relative;
  display: flex;
  justify-content: center;
  margin: 20px 0;
}

.btn-refresh {
  padding: 8px 16px;
  font-size: 14px;
  font-weight: bold;
  cursor: pointer;
  border: none;
  border-radius: 5px;
  background-color: #ef4444; /* 初始红色 */
  color: #fff;
  transition: background-color 0.5s ease;
}

.btn-refresh:disabled {
  cursor: not-allowed;
  opacity: 0.7;
}

.btn-refresh-success {
  background-color: #22c55e; /* 成功绿色 */
}

.message-bubble {
  position: absolute;
  bottom: 100%; /* 气泡位于按钮上方 */
  margin-bottom: 10px; /* 与按钮保持 10px 间距 */
  padding: 8px 12px;
  border-radius: 6px;
  background-color: #22c55e;
  color: #fff;
  font-size: 14px;
  white-space: nowrap;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: opacity 1s ease;
  z-index: 999; /* 确保图层最高 */
}

/* 气泡小三角 */
.message-bubble::before {
  content: '';
  position: absolute;
  top: 100%; /* 小三角位于气泡底部 */
  left: 50%;
  transform: translateX(-50%);
  border-width: 6px;
  border-style: solid;
  border-color: #22c55e transparent transparent transparent; /* 小三角朝下 */
}
</style>
