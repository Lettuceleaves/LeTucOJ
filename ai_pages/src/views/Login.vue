<template>
  <main class="login-container">
    <h1>登录</h1>
    <form @submit.prevent="login">
      <div>
        <label for="username">英文用户名：</label>
        <input type="text" id="username" v-model="username" required />
      </div>
      <div>
        <label for="password">密码：</label>
        <input type="password" id="password" v-model="password" required />
      </div>
      <button type="submit" class="submit-btn">登录</button>
      <button type="button" class="aux-btn" @click="goHome">
        返回主页
      </button>
    </form>
  </main>
</template>

<script setup>
import { ref, getCurrentInstance  } from 'vue';
import { useRouter } from 'vue-router';

const username = ref('');
const password = ref('');
const router = useRouter();

const goHome = () => router.push('/')

const instance = getCurrentInstance()
const ip = instance.appContext.config.globalProperties.$ip

const login = async () => {
  try {
    const res = await fetch(`http://${ip}/user/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        username: username.value,
        password: password.value
      })
    });

    // 先把响应体一次性读出来
    const data = await res.json();

    if (data.code === '0') {
      router.push('/main');
      return;
    } else if (data.code === 'B070004' || data.code === 'B070003') {
      alert(data.message);
      return;
    } else {
      alert('网页错误，请联系开发者');
      return;
    }
  } catch (err) {
    alert('登录失败：网络错误或服务器未响应');
  }
};

</script>
<style scoped>
/* ========== 容器 & 基础样式 (保持简洁风格) ========== */
.login-container {
  max-width: 380px; /* 宽度与注册页面保持一致 */
  margin: 50px auto;
  padding: 40px; /* 填充与注册页面一致 */
  background: #ffffff;
  border-radius: 12px; /* 圆角与注册页面一致 */
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  border: 1px solid #e0e0e0; 
  font-family: "Helvetica Neue", Helvetica, Arial, "Microsoft YaHei", sans-serif;
}

.login-container h1 {
  text-align: center;
  margin-bottom: 30px;
  font-size: 24px;
  color: #333333;
  font-weight: 600;
}

/* ========== 表单元素 ========== */
.login-container form div {
  margin-bottom: 20px;
}

.login-container label {
  display: block;
  margin-bottom: 8px;
  font-size: 14px;
  color: #4a4a4a;
  font-weight: 500;
}

.login-container input {
  width: 100%;
  padding: 12px 14px;
  border: 1px solid #cccccc;
  border-radius: 8px;
  box-sizing: border-box;
  font-size: 16px;
  color: #333;
  outline: none;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.login-container input:focus {
  border-color: #007bff; 
  box-shadow: 0 0 0 3px rgba(0, 123, 255, 0.25);
}

/* 统一按钮基础外观 - 确保 submit 和 aux 按钮继承 */
.login-container button {
  width: 100%;
  padding: 12px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 16px;
  font-weight: 600;
  transition: all 0.2s ease;
}

/* ⭐ 1. 提交按钮 (主操作 - 蓝色) ⭐ */
.submit-btn {
  margin-top: 10px; /* 紧跟输入框 */
  background-color: #007bff;
  color: #fff;
}
.submit-btn:hover {
  background-color: #0056b3;
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

/* ⭐ 2. 辅助按钮 (返回 - 次要操作) ⭐ */
.aux-btn {
  margin-top: 15px; /* 与提交按钮保持距离 */
  background-color: transparent; 
  color: #007bff; 
  font-size: 14px;
  font-weight: 500; /* 略微减弱字重 */
  padding: 10px; /* 略微减小内边距 */
}

.aux-btn:hover {
  background-color: #e9f5ff; /* 浅蓝色背景反馈 */
  color: #0056b3;
  transform: translateY(0); /* 确保不浮动 */
  box-shadow: none;
}

/* 移除旧的样式残留，这里不需要 :deep，因为我们更改了类名 */
/* 旧的 .btn-back 样式已被新的 .aux-btn 样式取代 */
</style>