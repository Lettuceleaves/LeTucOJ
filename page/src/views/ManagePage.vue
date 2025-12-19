<template>
  <div class="admin-dashboard">
    <h2>管理中心</h2>

    <div class="card-grid">
      <div class="admin-card user-management" @click="goToPage('/admin/users')">
        <div class="card-icon">👥</div>
        <h3>用户列表与权限管理</h3>
        <p>查看所有用户、修改角色、启用/禁用账号。</p>
      </div>

      <div class="admin-card db-refresh" @click="goToPage('/admin/db')">
        <div class="card-icon">💾</div>
        <h3>数据备份</h3>
        <p>执行数据库快照备份</p>
      </div>

      <div class="admin-card history-log" @click="goToPage('/admin/history')">
        <div class="card-icon">📜</div>
        <h3>系统日志</h3>
        <p>查看全部提交记录、指定用户的提交记录</p>
      </div>
    </div>
  </div>
</template>

<script>
// 假设这是一个 Vue 3 组件 (使用 <script setup> 更简洁，这里用 Options API 示例)
export default {
  name: 'AdminDashboard',
  methods: {
    /**
     * 跳转到指定的路由路径
     * @param {string} path 要跳转的路由路径
     */
    goToPage(path) {
      // 使用 Vue Router 实例进行导航
      // 确保你的 Vue 实例已经安装并使用了 Vue Router
      if (this.$router) {
        this.$router.push(path).catch(err => {
          // 捕获并处理可能的导航错误（例如，跳转到当前路径）
          if (err.name !== 'NavigationDuplicated') {
            console.error('路由跳转失败:', err);
          }
        });
      } else {
        console.error('Vue Router 实例 ($router) 未找到。请确保组件已正确配置路由。');
      }
    }
  }
};
</script>

<style scoped>
.admin-dashboard {
  max-width: 900px;
  margin: 40px auto;
  padding: 0 20px;
  text-align: center;
}

h2 {
  margin-bottom: 40px;
  font-size: 28px;
  color: #333;
}

.card-grid {
  display: flex;
  /* 🌟 修改：允许卡片在必要时换行 */
  flex-wrap: wrap;
  justify-content: center;
  gap: 30px;
}

.admin-card {
  /* 🌟 修改：使三个卡片能均匀分配宽度，大约 30% */
  flex: 1 1 calc(33.33% - 20px);
  min-width: 250px;
  height: 220px;
  padding: 25px;
  border-radius: 12px;
  background-color: #fff;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: transform 0.3s, box-shadow 0.3s;
  text-align: left;
  border: 1px solid #e0e0e0;
}

.admin-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 30px rgba(25, 118, 210, 0.2);
}

/* 模块特定的颜色 */
.user-management {
  border-left: 5px solid #1e88e5; /* 蓝色标识 */
}

.db-refresh {
  border-left: 5px solid #4caf50; /* 绿色标识 */
}

/* 🌟 新增：历史记录模块的颜色标识 */
.history-log {
  border-left: 5px solid #ff9800; /* 橙色/黄色标识 */
}

.card-icon {
  font-size: 40px;
  margin-bottom: 15px;
  line-height: 1;
}

h3 {
  margin-top: 0;
  color: #333;
  font-size: 20px;
  margin-bottom: 10px;
}

p {
  color: #777;
  font-size: 14px;
  line-height: 1.5;
}
</style>