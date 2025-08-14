<template>
  <div class="user-page">
    <h2>用户列表</h2>

    <!-- 加载 / 错误 / 空状态 -->
    <div v-if="loading" class="tip">加载中，请稍候…</div>
    <div v-else-if="error" class="tip error">{{ error }}</div>
    <div v-else-if="users.length === 0" class="tip">暂无用户</div>

    <!-- 列表 -->
    <table v-else class="user-table">
      <!-- 表头 -->
      <thead>
        <tr>
          <th>用户名</th>
          <th>中文名</th>
          <th>角色</th>
          <th>启用状态</th>
          <th v-if="isAdmin">操作</th>
        </tr>
      </thead>

      <!-- 表格行 -->
      <tbody>
        <tr v-for="u in users" :key="u.userName">
          <td>{{ u.userName }}</td>
          <td>{{ u.cnname || '-' }}</td>
          <td>{{ u.role }}</td>
          <td>{{ u.enabled ? '启用' : '禁用' }}</td>

          <!-- 操作列 -->
          <td v-if="isAdmin">
            <!-- 角色相关 -->
            <button v-if="u.role === 'USER'" @click="upgrade(u)" class="btn-role">升级</button>
            <button v-if="u.role === 'MANAGER'" @click="downgrade(u)" class="btn-role">降级</button>

            <!-- 状态相关 -->
            <button v-if="u.enabled" @click="disable(u)" class="btn-status">禁用</button>
            <button v-else @click="enable(u)" class="btn-status">启用</button>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, getCurrentInstance, computed } from 'vue'

const instance = getCurrentInstance()

const REMOTE_SERVER_URL = import.meta.env.VITE_REMOTE_SERVER_URL

const role = ref('')
const name = ref('') // 当前登录用户名

/* ================== 计算属性：是否是管理员 ================== */
const isAdmin = computed(() => {
  const r = role.value
  return r === 'ROOT' || r === 'MANAGER'
})

/* ================== 升级 / 降级 ================== */
const token = () => localStorage.getItem('jwt') || ''

/* 统一调用封装（params 形式） */
const call = (endpoint: string, userName: string) =>
  fetch(`${REMOTE_SERVER_URL}${endpoint}?pname=${encodeURIComponent(userName)}`, {
    method: 'PUT',
    headers: { Authorization: `Bearer ${token()}` },
  })
    .then((r) => r.json())
    .then((j) => {
      if (j.status !== 0) throw new Error(j.error || '操作失败')
      return fetchUsers()
    })
    .catch((e) => alert(e.message || '网络错误'))

/* 四个按钮对应的方法 */
const upgrade = (u: UserDTO) => call('/user/promote', u.userName)
const downgrade = (u: UserDTO) => call('/user/demote', u.userName)
const enable = (u: UserDTO) => call('/user/activate', u.userName)
const disable = (u: UserDTO) => call('/user/deactivate', u.userName)

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

const parseRole = () => {
  const token = localStorage.getItem('jwt')
  if (!token) return
  try {
    const payload = parseJwt(token)
    role.value = payload.role || ''
    name.value = payload.sub || ''
  } catch {
    alert('解析角色失败')
  }
}

/* ========== 类型定义（与后端一致） ========== */
interface UserDTO {
  userName: string
  cnname: string
  password: string // 前端不展示，但保留字段
  role: string
  enabled: boolean
}

/* ========== 响应式数据 ========== */
const users = ref<UserDTO[]>([])
const loading = ref(true)
const error = ref<string | null>(null)

/* ========== 拉取数据 ========== */
const fetchUsers = async () => {
  loading.value = true
  error.value = null
  const token = localStorage.getItem('jwt')

  try {
    /* 1. 普通用户：status=0 或 1 都认为是成功 */
    const res = await fetch(`${REMOTE_SERVER_URL}/user/users`, {
      method: 'GET',
      headers: { Authorization: `Bearer ${token}` },
    })
    const json = await res.json()
    if (![0, 1].includes(json.status)) throw new Error(json.error || '拉取用户列表失败')
    let list: UserDTO[] = json.data ?? []

    /* 2. 管理员额外合并 managers，同样把 status=1 当空 */
    if (role.value === 'ROOT' || role.value === 'MANAGER') {
      const mgrRes = await fetch(`${REMOTE_SERVER_URL}/user/managers`, {
        method: 'GET',
        headers: { Authorization: `Bearer ${token}` },
      })
      const mgrJson = await mgrRes.json()
      if ([0, 1].includes(mgrJson.status) && Array.isArray(mgrJson.data)) {
        list = [...list, ...mgrJson.data]
      } else if (![0, 1].includes(mgrJson.status)) {
        throw new Error(mgrJson.error || '拉取 manager 列表失败')
      }
    }

    /* 3. 去重 */
    const map = new Map<string, UserDTO>()
    list.forEach((u) => map.set(u.userName, u))
    users.value = Array.from(map.values())
  } catch (e: any) {
    error.value = e.message || '网络错误'
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  parseRole()
  fetchUsers()
})
</script>

<style scoped>
.btn-role,
.btn-status {
  padding: 4px 8px;
  margin-right: 6px;
  font-size: 12px;
  cursor: pointer;
}
.btn-role {
  background: #409eff;
  color: #fff;
  border: none;
  border-radius: 3px;
}
.btn-status {
  background: #f56c6c;
  color: #fff;
  border: none;
  border-radius: 3px;
}

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

/* 提示 */
.tip {
  text-align: center;
  padding: 20px;
  font-size: 16px;
}
.tip.error {
  color: #b91c1c;
}

/* 表格 */
.user-table {
  width: 100%;
  border-collapse: collapse;
}
.user-table th,
.user-table td {
  padding: 8px 12px;
  border: 1px solid #e5e7eb;
  text-align: left;
}
.user-table th {
  background-color: #f9fafb;
}
</style>
