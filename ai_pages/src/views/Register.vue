<template>
  <div class="register-container">
    <h2>注册</h2>
    <form @submit.prevent="register">
      <div>
        <label>用户名（3 位以上英文）：</label>
        <input v-model="username" type="text" maxlength="20" />
      </div>

      <div>
        <label>中文名：</label>
        <input v-model="cnname" type="text" maxlength="50" />
      </div>

      <div>
        <label>密码（≥6 位）：</label>
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
import { ref, getCurrentInstance } from 'vue'

const username      = ref('')
const cnname        = ref('')
const password      = ref('')
const confirmPassword = ref('')

const { appContext } = getCurrentInstance()
const ip = appContext.config.globalProperties.$ip

/* 注册 */
const register = async () => {
  // 1. 用户名：3 位以上纯英文
  if (!/^[A-Za-z]{3,}$/.test(username.value.trim())) {
    alert('用户名需为 3 位以上英文字母')
    return
  }

  // 2. 中文名：至少 1 位
  if (!cnname.value.trim()) {
    alert('请输入中文名')
    return
  }

  // 3. 密码：≥6 位
  if (password.value.trim().length < 6) {
    alert('密码至少 6 位')
    return
  }

  // 4. 两次密码一致
  if (password.value.trim() !== confirmPassword.value.trim()) {
    alert('两次输入的密码不一致')
    return
  }

  try {
    const data = await fetch(`http://${ip}/user/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        username: username.value.trim(),
        password: password.value.trim(),
        cnname: cnname.value.trim()
      })
    })

    const json = await data.json();

    if (json.status === 0) {
      alert('注册成功！')
      window.location.href = '/login'
    } else {
      alert('注册失败：' + (json.error || '未知错误'))
    }
  } catch (e) {
    alert('请求失败：' + (e.response?.json?.error || e.message || '网络错误'))
  }
}
</script>

<style scoped>
.register-container {
  width: 320px;
  margin: auto;
  padding: 20px;
}
label {
  display: block;
  margin-top: 10px;
}
input {
  width: 100%;
  box-sizing: border-box;
}
button {
  margin-top: 15px;
  width: 100%;
}
</style>
