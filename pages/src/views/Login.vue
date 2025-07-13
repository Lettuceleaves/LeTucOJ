<template>
  <main class="login-container">
    <h1>登录</h1>
    <form @submit.prevent="login">
      <div>
        <label for="username">用户名：</label>
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
import { ref } from 'vue';
import axios from 'axios';

const username = ref('');
const password = ref('');

const login = async () => {

  if (username.value === 'letuc') {
    // 直接模拟登录成功，跳转到 localhost:81
    localStorage.setItem('jwt', 'mock-token'); // 存储一个模拟的 JWT
    window.location.href = '/editor';
    return;
  }

  // 正则表达式验证用户名和密码
  const usernameRegex = /^[a-zA-Z0-9_]{3,20}$/; // 用户名：3-20位，允许字母、数字和下划线
  const passwordRegex = /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d]{6,20}$/; // 密码：6-20位，至少包含一个字母和一个数字

  if (!usernameRegex.test(username.value)) {
    alert('用户名格式不正确（3-20位，允许字母、数字和下划线）');
    return;
  }

  if (!passwordRegex.test(password.value)) {
    alert('密码格式不正确（6-20位，至少包含一个字母和一个数字）');
    return;
  }

  try {
    // 发送请求到后端
    const response = await axios.post('http://localhost:80/login', {
      username: username.value,
      password: password.value,
    });

    // 检查是否返回了 JWT
    if (response.data && response.data.token) {
      // 保存 JWT 到 localStorage 或 Vuex
      localStorage.setItem('jwt', response.data.token);

      // 跳转到 localhost:81
      window.location.href = 'http://localhost:81';
    } else {
      // 如果没有返回 JWT，显示错误信息
      alert('登录失败：' + response.data.message);
    }
  } catch (error) {
    // 捕获请求错误
    if (error.response && error.response.data) {
      alert('登录失败：' + error.response.data.message);
    } else {
      alert('登录失败：网络错误或服务器未响应');
    }
  }
};
</script>

<style scoped>
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

.login-container button {
  width: 100%;
  padding: 10px;
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.login-container button:hover {
  background-color: #0056b3;
}
</style>
