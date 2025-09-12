<template>
  <div class="register-container">
    <div class="register-card">
      <h2 class="title">注册</h2>
      <form @submit.prevent="register" class="form">
        <div class="form-group">
          <label>用户名（3 位以上英文）：</label>
          <input v-model="username" type="text" maxlength="20" />
        </div>

        <div class="form-group">
          <label>中文名：</label>
          <input v-model="cnname" type="text" maxlength="50" />
        </div>

        <div class="form-group">
          <label>密码（≥6 位）：</label>
          <input v-model="password" type="password" />
        </div>

        <div class="form-group">
          <label>确认密码：</label>
          <input v-model="confirmPassword" type="password" />
        </div>

        <button type="submit" class="submit-btn">注册</button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, getCurrentInstance } from 'vue'
import { useRouter } from 'vue-router'

const username = ref('')
const cnname = ref('')
const password = ref('')
const confirmPassword = ref('')
const router = useRouter();

const { appContext } = getCurrentInstance()
const ip = appContext.config.globalProperties.$ip

/* 注册 */
const register = async () => {
  if (!/^[A-Za-z]{3,}$/.test(username.value.trim())) {
    alert('用户名需为 3 位以上英文字母')
    return
  }

  if (!cnname.value.trim()) {
    alert('请输入中文名')
    return
  }

  if (password.value.trim().length < 6) {
    alert('密码至少 6 位')
    return
  }

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

    const json = await data.json()

    if (json.status === 0) {
      alert('注册成功！')
      router.push('/main');
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
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: linear-gradient(135deg, #6a11cb, #2575fc);
  font-family: "Microsoft YaHei", sans-serif;
}

.register-card {
  width: 360px;
  padding: 30px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 8px 20px rgba(0,0,0,0.15);
  animation: fadeIn 0.6s ease;
}

.title {
  text-align: center;
  margin-bottom: 20px;
  color: #333;
}

.form-group {
  margin-bottom: 15px;
}

label {
  display: block;
  margin-bottom: 6px;
  font-size: 14px;
  color: #555;
}

input {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #ddd;
  border-radius: 8px;
  box-sizing: border-box;
  outline: none;
  transition: all 0.2s ease;
}

input:focus {
  border-color: #2575fc;
  box-shadow: 0 0 4px rgba(37,117,252,0.4);
}

.submit-btn {
  margin-top: 20px;
  width: 100%;
  padding: 12px;
  border: none;
  border-radius: 8px;
  background: linear-gradient(90deg, #6a11cb, #2575fc);
  color: #fff;
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 12px rgba(37,117,252,0.3);
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
