<template>
  <main class="register-container">
    <h1>注册</h1>
    <form @submit.prevent="register">
      <div>
        <label for="username">用户名：</label>
        <input type="text" id="username" v-model="username" required />
      </div>
      <div>
        <label for="password">密码：</label>
        <input type="password" id="password" v-model="password" required />
      </div>
      <div>
        <label for="confirm-password">确认密码：</label>
        <input type="password" id="confirm-password" v-model="confirmPassword" required />
      </div>
      <button type="submit">注册</button>
    </form>
  </main>
</template>

<script setup>
import { ref } from 'vue';
import axios from 'axios';

const username = ref('');
const password = ref('');
const confirmPassword = ref('');

const register = async () => {
  // 正则验证用户名
  const usernameRegex = /^[a-zA-Z0-9_]{5,20}$/;
  if (!usernameRegex.test(username.value)) {
    alert('用户名格式不正确：5-20 个字符，只能包含字母、数字和下划线');
    return;
  }

  // 正则验证密码
  const passwordRegex = /^(?=.*[a-zA-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,20}$/;
  if (!passwordRegex.test(password.value)) {
    alert('密码格式不正确：8-20 个字符，至少包含一个字母、一个数字和一个特殊字符');
    return;
  }

  // 确认密码是否一致
  if (password.value !== confirmPassword.value) {
    alert('两次输入的密码不一致');
    return;
  }

  try {
    // 发送注册请求到后端
    const response = await axios.post('http://localhost:80/register', {
      username: username.value,
      password: password.value,
    });

    // 根据后端返回的结果处理
    if (response.data && response.data.success) {
      alert('注册成功！');
      // 可以跳转到登录页面或其他页面
      window.location.href = '/login';
    } else {
      alert('注册失败：' + response.data.message);
    }
  } catch (error) {
    if (error.response && error.response.data) {
      alert('注册失败：' + error.response.data.message);
    } else {
      alert('注册失败：网络错误或服务器未响应');
    }
  }
};
</script>

<style scoped>
.register-container {
  max-width: 400px;
  margin: 50px auto;
  padding: 20px;
  border: 1px solid #ccc;
  border-radius: 8px;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
}

.register-container h1 {
  text-align: center;
  margin-bottom: 20px;
}

.register-container form div {
  margin-bottom: 10px;
}

.register-container label {
  display: block;
  margin-bottom: 5px;
}

.register-container input {
  width: 100%;
  padding: 8px;
  box-sizing: border-box;
}

.register-container button {
  width: 100%;
  padding: 10px;
  background-color: #28a745;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.register-container button:hover {
  background-color: #218838;
}
</style>
