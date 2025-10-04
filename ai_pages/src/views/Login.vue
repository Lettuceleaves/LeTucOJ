<template>
  <main class="login-container">
    <h1>登录</h1>
    <div class="back-wrap">
      <button class="btn-back" @click="goHome">返回主页</button>
    </div>
    <form @submit.prevent="login">
      <div>
        <label for="username">英文用户名：</label>
        <input type="text" id="username" v-model="username" required />
      </div>
      <div>
        <label for="password">密码：</label>
        <input type="password" id="password" v-model="password" required />
      </div>
      <button type="submit">登录</button>
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
    } else if (data.status === 'B070004') {
      alert(data.message);
      router.push('/');
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
/* ========== 容器 ========== */
.login-container {
  max-width: 400px;
  margin: 50px auto;
  padding: 20px;
  border: 1px solid #ccc;
  border-radius: 8px;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
}

.login-container h1 {
  text-align: center;
  margin-bottom: 20px;
}

/* ========== 表单元素 ========== */
.login-container form div {
  margin-bottom: 10px;
}

.login-container label {
  display: block;
  margin-bottom: 5px;
}

.login-container input {
  width: 100%;
  padding: 8px;
  box-sizing: border-box;
}

/* 统一按钮基础外观 */
.login-container button {
  width: 100%;
  padding: 10px;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
  transition: background-color 0.2s;
}

/* 登录按钮 */
.login-container button[type="submit"] {
  background-color: #007bff;
}
.login-container button[type="submit"]:hover {
  background-color: #0056b3;
}

/* 红色返回按钮（深度选择器保证 scoped 生效） */
:deep(.btn-back) {
  width: 100%;
  padding: 10px;
  margin-bottom: 15px;
  background-color: #ef4444;   /* Tailwind red-500 */
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
  transition: background-color 0.2s;
}
:deep(.btn-back:hover) {
  background-color: #dc2626;   /* Tailwind red-600 */
}
</style>
