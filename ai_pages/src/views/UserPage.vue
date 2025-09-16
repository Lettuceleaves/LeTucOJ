<!-- UserList.vue -->
<template>
  <div class="user-page">
    <h2>用户列表</h2>

    <!-- 一键筛选未启用用户 -->
    <div class="filter-bar">
      <label>
        <input type="checkbox" v-model="onlyDisabled" />
        仅显示未启用用户
      </label>
    </div>

    <!-- 加载 / 错误 / 空状态 -->
    <div v-if="loading" class="tip">加载中，请稍候…</div>
    <div v-else-if="error" class="tip error">{{ error }}</div>
    <div v-else-if="displayUsers.length === 0" class="tip">暂无用户</div>

    <!-- 表格外壳，限定高度并允许滚动 -->
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
            <td>{{ u.enabled ? '启用' : '禁用' }}</td>

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
                v-if="u.enabled"
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

<script setup lang="ts">
import { ref, computed, onMounted, getCurrentInstance } from 'vue';

/* ---------- 全局配置 ---------- */
const instance = getCurrentInstance();
const ip = instance!.appContext.config.globalProperties.$ip;

const role = ref<string>('');
const name = ref<string>('');          // 当前登录用户名
const token = () => localStorage.getItem('jwt') || '';

/* ---------- 角色解析 ---------- */
const parseJwt = (tk: string) => {
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

/* ---------- 类型定义 ---------- */
interface UserDTO {
  userName: string;
  cnname: string;
  password: string;
  role: string;
  enabled: boolean;
}

/* ---------- 响应式数据 ---------- */
const users       = ref<UserDTO[]>([]);
const loading     = ref<boolean>(true);
const error       = ref<string | null>(null);
const onlyDisabled = ref<boolean>(false);        // 筛选开关

/* ---------- 显示列表（过滤后） ---------- */
const displayUsers = computed(() =>
  onlyDisabled.value
    ? users.value.filter((u) => !u.enabled)
    : users.value
);

/* ---------- 通用请求封装 ---------- */
const call = (endpoint: string, userName: string) =>
  fetch(`http://${ip}${endpoint}?pname=${encodeURIComponent(userName)}`, {
    method: 'PUT',
    headers: { Authorization: `Bearer ${token()}` },
  })
    .then((r) => r.json())
    .then((j) => {
      if (j.status !== 0) throw new Error(j.error || '操作失败');
      return fetchUsers();   // 刷新列表
    })
    .catch((e) => alert(e.message || '网络错误'));

/* ---------- 四个按钮 ---------- */
const upgrade   = (u: UserDTO) => call('/user/promote', u.userName);
const downgrade = (u: UserDTO) => call('/user/demote', u.userName);
const enable    = (u: UserDTO) => call('/user/activate', u.userName);
const disable   = (u: UserDTO) => call('/user/deactivate', u.userName);

/* ---------- 拉取用户列表 ---------- */
const fetchUsers = async () => {
  loading.value = true;
  error.value   = null;
  const tk = token();

  try {
    /* 1. 普通用户列表（status 0 或 1 都算成功） */
    const res = await fetch(`http://${ip}/user/users`, {
      method: 'GET',
      headers: { Authorization: `Bearer ${tk}` },
    });
    const json = await res.json();
    if (![0, 1].includes(json.status))
      throw new Error(json.error || '拉取用户列表失败');
    let list: UserDTO[] = json.data ?? [];

    /* 2. 管理员额外拉 manager 列表 */
    if (role.value === 'ROOT' || role.value === 'MANAGER') {
      const mgrRes = await fetch(`http://${ip}/user/managers`, {
        method: 'GET',
        headers: { Authorization: `Bearer ${tk}` },
      });
      const mgrJson = await mgrRes.json();
      if ([0, 1].includes(mgrJson.status) && Array.isArray(mgrJson.data)) {
        list = [...list, ...mgrJson.data];
      } else if (![0, 1].includes(mgrJson.status)) {
        throw new Error(mgrJson.error || '拉取 manager 列表失败');
      }
    }

    /* 3. 按用户名去重 */
    const map = new Map<string, UserDTO>();
    list.forEach((u) => map.set(u.userName, u));
    users.value = Array.from(map.values());
  } catch (e: any) {
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
  border: 1px solid #e5e7eb;
  text-align: left;
}
.user-table th {
  background-color: #f9fafb;
  position: sticky;
  top: 0;
  z-index: 1;
}
</style>
