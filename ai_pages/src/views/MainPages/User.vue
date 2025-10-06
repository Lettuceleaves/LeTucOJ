<template>
  <div class="user-profile-container">

    <div class="profile-header-background"
      :style="{ backgroundImage: 'url(' + backgroundImageUrl + ')', height: backgroundHeight + 'px' }">
      <div v-if="loading" class="loading-overlay">加载中...</div>
      <div v-else-if="error" class="error-overlay" @click.stop="fetchUserInfo">
        加载失败，点击重试
      </div>
      <div class="resize-bar" @mousedown.stop="startDrag"></div>
    </div>

    <label class="bg-change-btn-outer"
          :style="{ top: backgroundHeight + 8 + 'px' }">
      <input type="file" accept=".jpg,.jpeg" @change="onSelectBg" style="display: none;">
      👆🏻
    </label>

    <div class="profile-content">
      <div class="avatar-wrapper">
        <div class="avatar-area">
          <img :src="avatarUrl"
              :alt="userInfo.cnname || userInfo.userName"
              class="avatar-placeholder">
        </div>

        <label class="upload-trigger">
          <input ref="fileInput"
                type="file"
                accept=".jpg,.jpeg"
                @change="onSelectFile">
          👈🏻
        </label>
      </div>

      <div class="info-area">
        <div class="header-and-button">
            <div>
                <h2>{{ userInfo.cnname || userInfo.userName || '加载中...' }}</h2>
                <p class="user-id">ID: {{ userInfo.userName || '...' }}</p>
            </div>
            <button class="edit-profile-btn" @click="openModal">
                修改资料
            </button>
        </div>
        
        <div class="contact-info">
          <p class="description">
            <span class="label">描述：</span>
            {{ userInfo.description || '这个人很懒，什么都没有留下...' }}
          </p>
          <p><span class="label">手机：</span> {{ userInfo.phone || '暂无' }}</p>
          <p><span class="label">邮箱：</span> {{ userInfo.email || '暂无' }}</p>
        </div>
      </div>

      <div class="heatmap-placeholder">
        <h3>活动热力图 TODO</h3>
      </div>
      
      <hr class="section-divider">

      <div class="history-list-placeholder">
        <h3>历史记录 TODO</h3>
        <p>这里将显示详细的历史记录</p>
      </div>
    </div>
  </div>

  <div class="modal-overlay" v-if="isModalVisible">
      <div class="modal-content">
        <h3>编辑个人资料</h3>
        <form @submit.prevent="submitForm">
          
          <div class="form-group">
            <label for="cnname">中文姓名</label>
            <input id="cnname" type="text" v-model="editForm.cnname" required>
          </div>

          <div class="form-group">
            <label for="description">个人描述</label>
            <textarea id="description" v-model="editForm.description" rows="3"></textarea>
          </div>

          <div class="form-group">
            <label for="phone">手机号码</label>
            <input id="phone" type="text" v-model="editForm.phone">
          </div>

          <div class="form-group">
            <label for="email">邮箱</label>
            <input id="email" type="email" v-model="editForm.email">
          </div>

          <div class="modal-actions">
            <button type="button" class="btn-cancel" @click="closeModal">取消</button>
            <button type="submit" class="btn-submit">保存</button>
          </div>
        </form>
      </div>
    </div>
</template>

<script setup>
// ... (JavaScript 部分保持不变) ...
import { ref, onMounted, getCurrentInstance } from 'vue';

// ===================================
//             配置项
// ===================================

// 默认占位图（如果后端不提供，或者加载失败）
const DEFAULT_AVATAR = 'path/to/default/avatar.png';  
const DEFAULT_BACKGROUND = 'path/to/default/background.jpg';  

// ===================================
//             状态和数据
// ===================================

const loading = ref(true);
const error = ref(false);

// 后端模型映射
const userInfo = ref({
  userName: '',
  cnname: '',
  email: '',
  phone: '',
  description: ''
});

const isModalVisible = ref(false);
const editForm = ref({}); // 用于绑定表单的临时数据

// 图片URL（通常需要单独的接口或逻辑获取）
const avatarUrl = ref(DEFAULT_AVATAR);
const backgroundImageUrl = ref(DEFAULT_BACKGROUND);

const instance = getCurrentInstance()
const ip = instance.appContext.config.globalProperties.$ip

const bgInput = ref(null)

// ===================================
//             业务逻辑 (Modal & Form)
// ===================================

function openModal() {
  // 1. 将当前用户信息复制到编辑表单中，避免直接修改原始数据
  editForm.value = { ...userInfo.value }; 
  // 2. 显示 Modal
  isModalVisible.value = true;
}

function closeModal() {
  isModalVisible.value = false;
}

async function submitForm() {
  try {
    const token = localStorage.getItem('jwt');
    const payload = parseJwt(token);
    const userName = payload.sub;
    
    // 确保 Content-Type: application/json 被设置
    const requestBody = {
      userName: userName,
      cnname: editForm.value.cnname,
      email: editForm.value.email,
      phone: editForm.value.phone,
      description: editForm.value.description
    };

    const res = await fetch(`http://${ip}/user/info/update`, {
      method: 'PUT',
      headers: { 
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(requestBody)
    });
    
    // 检查 HTTP 状态码是否成功，防止网络错误被漏掉
    if (!res.ok) {
        throw new Error(`HTTP 错误！状态码: ${res.status}`);
    }
    
    const response = await res.json(); // ⭐ 3. await res.json()
    
    if (response.code === '0') {
      // 更新本地状态，确保主界面数据同步
      userInfo.value = { ...editForm.value }; 
      alert('资料更新成功！');
    } else {
      // API 返回的业务逻辑错误
      throw new Error("API返回错误: " + (response.message || '未知错误'));
    }
    
  } catch (err) {
    // 捕获所有错误并处理
    console.error('更新用户信息失败:', err);
    alert('更新失败: ' + (err.message || '网络或服务异常'));
  } finally {
    // 无论成功还是失败，都关闭 Modal
    closeModal();
    // 这里的 loading.value = false; 可能不需要，因为你只在 fetchUserInfo 里用它
  }
}

// ===================================
//             数据获取
// ===================================

const parseJwt = (tk) => {
  try {
    // JWT 格式: header.payload.signature
    const base64Url = tk.split('.')[1];
    if (!base64Url) return {};
    
    // 替换 base64url 格式中的特殊字符为 base64 格式
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    
    // 使用 atob (Base64 解码) 和 decodeURIComponent 解码 payload
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split('')
        .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    );
    return JSON.parse(jsonPayload);
  } catch (e) {
    console.error("JWT解析失败:", e);
    return {};
  }
};

async function fetchUserInfo() {
  loading.value = true;
  error.value = false;
  
  try {
    const token = localStorage.getItem('jwt')
    const payload = parseJwt(token);
    const userName = payload.sub;
    const params = new URLSearchParams({
      pname: userName,
    }).toString();
    const res = await fetch(`http://${ip}/user/info/get?${params}`, {
      method: 'GET',
      headers: { Authorization: `Bearer ${token}` },
    })
    const response = await res.json()
    
    if (response.code === '0') {
      userInfo.value = response.data;
    } else {
      // 即使请求成功，数据也可能是空的或不符合预期
      throw new Error("API返回数据结构错误");
    }
    
    loading.value = false;
  } catch (err) {
    console.error('获取用户信息失败:', err);
    error.value = true;
    loading.value = false;
  }
}

/**
 * 抓取用户头像数据（原始二进制数据 Blob）并转换为 Object URL
 * @param {string} userName - 用户名
 */
async function fetchUserAvatar() {
    try {
        const token = localStorage.getItem('jwt')
        const payload = parseJwt(token);
        const userName = payload.sub;
        const params = new URLSearchParams({
          pname: userName,
        }).toString();
        const res = await fetch(`http://${ip}/user/headPortrait/get?${params}`, {
          method: 'GET',
          headers: { Authorization: `Bearer ${token}` },
        })
        const response = await res.json()
        if (response.code === '0') {
          avatarUrl.value = `data:image/jpeg;base64,${response.data}`
        } else {
          alert('获取用户头像失败: ' + (response.message || '未知错误'));
        }
    } catch (err) {
        console.error('获取用户头像失败:', err);
    }
}

const fileInput = ref(null)      // 指向隐藏 input

// 选择文件回调
async function onSelectFile(e) {
  const file = e.target.files[0]
  if (!file) return

  // 类型 & 大小校验
  const isJpg = file.type === 'image/jpeg'
  const isLt1M = file.size / 1024 / 1024 < 1

  if (!isJpg) {
    alert('只能上传 JPG 文件')
    return
  }
  if (!isLt1M) {
    alert('图片必须小于 1MB')
    return
  }

  // 构造 FormData 并上传
  const formData = new FormData()
  formData.append('file', file)

  try {
    const token = localStorage.getItem('jwt')
    const payload = parseJwt(token);
    const userName = payload.sub;
    const params = new URLSearchParams({
      pname: userName,
    }).toString();
    const res = await fetch(`http://${ip}/user/headPortrait/update?${params}`, {
      method: 'PUT',
      headers: { Authorization: `Bearer ${token}` },
      body: file
    })
    const json = await res.json()
    if (json.code === '0') {
      // 假设后端返回 { code:'0', data:'/9j/4AAQ...' }
      avatarUrl.value = `data:image/jpeg;base64,${json.data}`
      fetchUserAvatar();
    } else {
      alert('上传失败：' + (json.message || '未知错误'))
    }
  } catch (e) {
    console.error(e)
    alert('网络异常，上传失败')
  }

  // 清空 input，允许重复选同一张图
  fileInput.value.value = ''
}

async function onSelectBg(e) {
  const file = e.target.files[0]
  if (!file) return
  if (!file.type.includes('jpeg')) return alert('只能上传 JPG 背景')
  if (file.size > 1_048_576) return alert('背景图必须 < 1MB')

  try {
    const token = localStorage.getItem('jwt')
    const payload = parseJwt(token);
    const userName = payload.sub;
    const params = new URLSearchParams({
      pname: userName,
    }).toString();
    const res = await fetch(`http://${ip}/user/background/update?pname=${userName}`, {
      method: 'PUT',
      headers: { Authorization: `Bearer ${token}` },
      body: file
    })
    const json = await res.json()
    if (json.code === '0') {
      backgroundImageUrl.value = `data:image/jpeg;base64,${json.data}`
      fetchUserBackground()
    } else {
      alert('背景上传失败：' + (json.message || '未知错误'))
    }
  } catch (e) {
    console.error(e)
    alert('网络异常')
  }
  e.target.value = '' // 允许重复选同一张
}

// 手动打开文件选择框
function openBgSelect() {
  bgInput.value.click()
}

// 获取用户背景图
async function fetchUserBackground() {
  try {
    const token = localStorage.getItem('jwt')
    const payload = parseJwt(token)
    const userName = payload.sub
    const params = new URLSearchParams({ pname: userName }).toString()
    const res = await fetch(`http://${ip}/user/background/get?${params}`, {
      headers: { Authorization: `Bearer ${token}` }
    })
    const json = await res.json()
    if (json.code === '0' && json.data) {
      backgroundImageUrl.value = `data:image/jpeg;base64,${json.data}`
    } else {
      // 没上传过背景就保持默认图，不弹错
      console.log('未找到用户背景，使用默认')
    }
  } catch (e) {
    console.error('获取背景失败:', e)
  }
}
// 背景高度（默认 200，后续可从后端读）
const backgroundHeight = ref(200)

let dragStartY = 0      // 按下时鼠标 Y
let dragStartHeight = 0 // 按下时背景高度
let dragging = false    // 是否正在拖

// 按下拖动条
function startDrag(e) {
  dragging = true
  dragStartY = e.clientY
  dragStartHeight = backgroundHeight.value
  document.addEventListener('mousemove', onDrag)
  document.addEventListener('mouseup', stopDrag)
  document.body.style.cursor = 'ns-resize'  // 拖动时鼠标样式
}

// 拖动中
function onDrag(e) {
  if (!dragging) return
  const delta = e.clientY - dragStartY      // 向下为正
  let newHeight = dragStartHeight + delta
  // 限制最小 120，最大 500
  newHeight = Math.max(120, Math.min(500, newHeight))
  backgroundHeight.value = newHeight
}

// 松开
function stopDrag() {
  dragging = false
  document.removeEventListener('mousemove', onDrag)
  document.removeEventListener('mouseup', stopDrag)
  document.body.style.cursor = ''

  // 可选：把最终高度调接口存后端
  // await saveBgHeight(backgroundHeight.value)
}


// ===================================
//             生命周期
// ===================================

onMounted(() => {
  fetchUserInfo();
  fetchUserAvatar();
  fetchUserBackground()
});
</script>

<style scoped>
/* (略去大部分样式，只补充与加载状态相关的) */

/* 你原来的样式保持不变... */

.profile-header-background {
  height: 200px;  
  width: 100%;
  position: relative;
  background-color: #a0cfff;  
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  
  /* 当有背景图 URL 时，确保背景色只作为过渡色 */
}

/* 加载状态覆盖层 */
.loading-overlay, .error-overlay {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    display: flex;
    justify-content: center;
    align-items: center;
    font-size: 16px;
    font-weight: bold;
    color: #fff;
}

.loading-overlay {
    background-color: rgba(0, 0, 0, 0.4);
}

.error-overlay {
    background-color: rgba(220, 38, 38, 0.6); /* 红色半透明 */
    cursor: pointer;
}

.avatar-wrapper {
  position: relative;  /* + 号参考此框定位 */
  width: 80px;
  height: 80px;
}

.avatar-area {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  overflow: hidden;    /* 只裁圆形头像 */
  border: 2px solid #fff;
  box-shadow: 0 0 4px rgba(0,0,0,.2);
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

/* + 号：相对于 avatar-wrapper 定位 */
.upload-trigger {
  position: absolute;
  right: -35px;
  bottom: 25px;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: #d4e9ff;
  color: #fff;
  font-size: 18px;
  line-height: 24px;
  text-align: center;
  cursor: pointer;
  box-shadow: 0 0 4px rgba(0,0,0,.3);
  transition: background .2s;
}
.upload-trigger:hover {
  background: #67c23a;
}

.upload-trigger input[type=file] {
  display: none;
}

.bg-upload-trigger:hover {
  background: rgba(0, 0, 0, 0.7);
}

.resize-bar {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 8px;
  background: rgba(0,0,0,.1);
  cursor: ns-resize;    /* 上下拖动光标 */
  transition: background .2s;
}
.resize-bar:hover {
  background: rgba(0,0,0,.3);
}

.bg-change-btn {
  position: absolute;
  right: 12px;
  bottom: 12px;           /* 背景图内侧右下 */
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: rgba(0,0,0,.5);
  color: #fff;
  font-size: 18px;
  line-height: 32px;
  text-align: center;
  cursor: pointer;
  transition: background .2s;
  z-index: 10;           /* 高于拖动条 */
}
.bg-change-btn:hover {
  background: rgba(0,0,0,.7);
}

.user-profile-container {
  position: relative;    /* 建立包含块 */
}

.bg-change-btn-outer {
  position: absolute;
  right: 12px;
  /* top 由模板动态计算：背景高度 + 8px 间距 */
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: rgb(232, 248, 255);
  color: #fff;
  font-size: 20px;
  line-height: 36px;
  text-align: center;
  cursor: pointer;
  transition: background .2s;
  z-index: 10;
}
.bg-change-btn-outer:hover {
  background: rgba(215, 234, 255, 0.7);
}

.profile-content {
  position: relative;
  margin: 40px 16px 16px;        /* 上移盖住背景底部 + 左右留边 */
  padding: 24px 20px;
  background: #ffffff;
  border-radius: 16px 16px 0 0;      /* 顶部大圆角 */
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  z-index: 1;
}

/* ⭐⭐⭐ 新增样式: 标题和按钮布局 ⭐⭐⭐ */
.header-and-button {
  display: flex;
  justify-content: space-between; /* 标题居左，按钮居右 */
  align-items: center; /* 垂直居中对齐 */
  margin-bottom: 15px; /* 与下方联系信息的间距 */
}

.edit-profile-btn {
  padding: 6px 15px;
  border: 1px solid #1e88e5; /* 蓝色边框 */
  background-color: #ffffff; /* 白色背景 */
  color: #1e88e5; /* 蓝色文字 */
  border-radius: 20px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s;
  flex-shrink: 0; /* 防止按钮被 h2 挤压 */
  margin-left: 20px; /* 与 h2 的间距 */
}

.edit-profile-btn:hover {
  background-color: #e3f2fd; /* 浅蓝背景 */
  color: #0d47a1;
  border-color: #0d47a1;
}

.edit-profile-btn:active {
  background-color: #bbdefb;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5); /* 半透明黑色 */
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000; /* 确保在最上层 */
}

/* 悬浮窗内容区域 */
.modal-content {
  background: #fff;
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  width: 90%;
  max-width: 500px; /* 限制最大宽度 */
}

.modal-content h3 {
  margin-top: 0;
  margin-bottom: 20px;
  border-bottom: 1px solid #eee;
  padding-bottom: 10px;
  font-weight: 600;
}

/* 表单组样式 */
.form-group {
  margin-bottom: 15px;
}

.form-group label {
  display: block;
  font-size: 14px;
  color: #555;
  margin-bottom: 5px;
  font-weight: 500;
}

.form-group input[type="text"],
.form-group input[type="email"],
.form-group textarea {
  width: 100%;
  padding: 10px;
  border: 1px solid #ccc;
  border-radius: 4px;
  box-sizing: border-box; /* 确保 padding 不增加总宽度 */
  font-size: 14px;
}

.form-group textarea {
    resize: vertical; /* 允许垂直拖动调整大小 */
}

/* 按钮容器 */
.modal-actions {
  display: flex;
  justify-content: flex-end; /* 按钮靠右对齐 */
  margin-top: 25px;
}

/* 提交和取消按钮基础样式 */
.btn-submit, .btn-cancel {
  padding: 8px 18px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 15px;
  transition: background-color 0.2s;
  margin-left: 10px;
}

/* 取消按钮 */
.btn-cancel {
  background-color: #f0f0f0;
  color: #333;
}
.btn-cancel:hover {
  background-color: #e0e0e0;
}

/* 提交按钮 */
.btn-submit {
  background-color: #1e88e5; /* 蓝色 */
  color: #fff;
}
.btn-submit:hover {
  background-color: #1565c0; /* 深蓝色 */
}

.user-id {
    margin-top: -10px; /* 向上微调，减少与 h2 的间距 */
    margin-bottom: 5px; /* 与下方信息的间距 */
    font-size: 14px; /* 小字号 */
    color: #888; /* 灰色 */
    font-weight: 400; /* 避免加粗 */
}

/* 确保 h2 的默认 margin 被覆盖或控制 */
.profile-content h2 {
    margin-top: 0;
    margin-bottom: 5px; /* 调整 h2 底部间距 */
}

/* 修正 .header-and-button，因为它现在包裹了 <button> 和 <div> */
.header-and-button {
    display: flex;
    justify-content: space-between;
    align-items: flex-start; /* 保持左侧内容顶部对齐 */
    margin-bottom: 15px;
}
</style>