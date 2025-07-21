<template>
  <div class="register-container">
    <h2>注册</h2>
    <form @submit.prevent="register">
      <div>
        <label>用户名：</label>
        <input v-model="username" type="text" />
      </div>
      <div>
        <label>密码：</label>
        <input v-model="password" type="password" />
      </div>
      <div>
        <label>确认密码：</label>
        <input v-model="confirmPassword" type="password" />
      </div>
      <button type="submit">注册</button>
    </form>
  </div>
</template>

<script setup>
import { ref, getCurrentInstance  } from 'vue'
import axios from 'axios'

const username = ref('')
const password = ref('')
const confirmPassword = ref('')

const instance = getCurrentInstance()
const ip = instance.appContext.config.globalProperties.$ip

// 注册方法
const register = async () => {
  const usernameRegex = /^[a-zA-Z0-9_]{5,20}$/
  if (!usernameRegex.test(username.value)) {
    alert('用户名格式不正确：5-20 个字符，只能包含字母、数字和下划线')
    return
  }

  const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^a-zA-Z0-9]).{8,20}$/
  if (!passwordRegex.test(password.value)) {
    alert('密码格式不正确：8-20 个字符，需包含大写字母、小写字母、数字和特殊字符')
    return
  }

  if (password.value !== confirmPassword.value) {
    alert('两次输入的密码不一致')
    return
  }

  try {
    const response = await axios.post(`http://${ip}:7777/user/register`, {
      username: username.value,
      password: password.value
    })

    const res = response.data

    if (res.status === 0) {
      alert('注册成功！')
      window.location.href = '/login'
    } else {
      alert('注册失败：' + (res.error || '未知错误'))
    }
  } catch (error) {
    alert('请求失败：' + (error.response?.data?.error || error.message || '网络错误'))
  }
}
</script>

<style scoped>
.register-container {
  width: 300px;
  margin: auto;
  padding: 20px;
}
label {
  display: block;
  margin-top: 10px;
}
button {
  margin-top: 15px;
}
</style>
