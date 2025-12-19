<template>
  <div class="refresh-container">
    <button @click="goBack" class="btn-back">返回</button>

    <div class="backup-area">
      <button
        @click="refreshSql"
        :class="['btn-refresh', { 'btn-refresh-success': refreshStatus === 'success' }]"
        :disabled="sqlLoading"
      >
        {{ sqlLoading ? '备份中...' : 'DB一键备份' }}
      </button>
      <div v-if="showMessage" class="message-bubble" :style="{ opacity: messageOpacity }">
        {{ message }}
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import apiService from '../../utils/api'

/* ---------- 响应式数据（保持不变） ---------- */
const refreshStatus = ref('default')
const showMessage = ref(false)
const message = ref('')
const messageOpacity = ref(0)
const sqlLoading = ref(false) // DB 刷新加载状态
const router = useRouter()

/* ---------- 刷新 SQL 方法（保持不变） ---------- */
const refreshSql = async () => {
  if (sqlLoading.value) return
  sqlLoading.value = true
  refreshStatus.value = 'default'

  try {
    // const res = await fetch(`http://${ip}/sys/mysqldump`, {
    //   method: 'GET',
    //   headers: { Authorization: `Bearer ${tk}` },
    // });
    const json = await apiService.sys.getMysqlDump()
    if (json.data.code === '0') {
      refreshStatus.value = 'success'
      message.value = json.data || '缓存刷新成功'
      showMessage.value = true
      messageOpacity.value = 1

      // 成功后显示气泡并淡出
      setTimeout(() => {
        messageOpacity.value = 0
        setTimeout(() => {
          showMessage.value = false
          refreshStatus.value = 'default'
        }, 1000)
      }, 3000)
    } else {
      throw new Error(json.message || '刷新失败')
    }
  } catch (e) {
    alert('DB备份失败: ' + (e.message || '网络错误'))
    refreshStatus.value = 'default'
  } finally {
    // 即使失败，也需要延迟清除 loading，让用户看到点击反馈
    setTimeout(() => {
      sqlLoading.value = false
    }, 500)
  }
}

const goBack = () => {
  // 返回到上一个页面，类似于浏览器后退
  router.back()
}
</script>

<style scoped>
/* 核心修改：调整布局，让备份区域居中，返回按钮绝对定位到左上角 */
.refresh-container {
  position: relative;
  display: flex;
  /* 🌟 修改：确保内容区域居中 */
  justify-content: center;
  align-items: flex-start;
  max-width: 800px;
  margin: 40px auto;
  padding: 0 20px;
  min-height: 50px;
}

.btn-back {
  padding: 8px 16px;
  font-size: 14px;
  cursor: pointer;
  border: 1px solid #ccc;
  border-radius: 12px;
  background-color: #f0f0f0;
  color: #333;
  transition: background-color 0.3s;
  position: absolute;
  top: 40px;
  right: 20px;
  z-index: 1;
}

/* 备份区域 */
.backup-area {
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 250px;
}

/* DB 备份按钮样式（保持不变） */
.btn-refresh {
  padding: 8px 16px;
  font-size: 14px;
  font-weight: bold;
  cursor: pointer;
  border: none;
  border-radius: 50px;
  background-color: #ef4444;
  color: #fff;
  transition: background-color 0.5s ease;
}

.btn-refresh:disabled {
  cursor: not-allowed;
  opacity: 0.7;
}

.btn-refresh-success {
  background-color: #22c55e;
}

/* 气泡样式（保持不变） */
.message-bubble {
  position: absolute;
  bottom: 100%;
  margin-bottom: 10px;
  padding: 8px 12px;
  border-radius: 6px;
  background-color: #22c55e;
  color: #fff;
  font-size: 14px;
  white-space: nowrap;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: opacity 1s ease;
  z-index: 999;
}

/* 气泡小三角（保持不变） */
.message-bubble::before {
  content: '';
  position: absolute;
  top: 100%;
  left: 50%;
  transform: translateX(-50%);
  border-width: 6px;
  border-style: solid;
  border-color: #22c55e transparent transparent transparent;
}
</style>
