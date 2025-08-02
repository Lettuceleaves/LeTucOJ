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
import { ref, getCurrentInstance  } from 'vue';
import axios from 'axios';
import { useRouter } from 'vue-router';

const username = ref('');
const password = ref('');
const router = useRouter();

const instance = getCurrentInstance()
const ip = instance.appContext.config.globalProperties.$ip

const login = async () => {
  try {
    const token = localStorage.getItem('jwt')
    const response = await axios.post(`http://${ip}:7777/user/login`, {
      username: username.value,
      password: password.value,
    });

    if (response.data && response.data.token) {
      // 存储 token
      localStorage.setItem('jwt', response.data.token);
      // 角色等信息如果后端没返回，可以省略
      router.push('/list');
    } else {
      alert('登录失败：后端未返回 token');
    }
  } catch (error) {
    if (error.response && error.response.data) {
      alert('登录失败：' + (error.response.data.error || '未知错误'));
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
