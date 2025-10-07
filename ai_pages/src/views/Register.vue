<template>
  <div class="register-container">
    <div class="register-card">
      <h2 class="title">注册</h2>
      <form @submit.prevent="register" class="form">
        <div class="form-group">
          <label>用户名（姓名首字母小写+学号）：</label>
          <input v-model="username" type="text" maxlength="20" />
        </div>

        <div class="form-group">
          <label>昵称：</label>
          <input v-model="cnname" type="text" maxlength="50" />
        </div>

        <div class="form-group">
          <label>密码（≥6 位英文或数字）：</label>
          <input v-model="password" type="password" />
        </div>

        <div class="form-group">
          <label>确认密码：</label>
          <input v-model="confirmPassword" type="password" />
        </div>

        <button type="submit" class="submit-btn">注册</button>

        <button type="button" class="back-btn" @click="goToLogin">
          返回主页
        </button>
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

const goToLogin = () => {
    router.push('/');
};

/* 注册 */
const register = async () => {
    // 1. 校验用户名
    // 要求：2-10位英文字母 + 12位数字 (总长 14-22 位)
    // 示例：user123456789012
    const usernamePattern = /^[A-Za-z]{2,10}\d{12}$/;
    if (!usernamePattern.test(username.value.trim())) {
        alert('用户名格式错误：需为 2-10 位英文字母开头，后跟 12 位数字');
        return;
    }

    const trimmedUsername = username.value.trim();
    const trimmedCnname = cnname.value.trim();
    const trimmedPassword = password.value.trim();

    // 2. 校验中文昵称 (长度 > 0 且 <= 20)
    if (!trimmedCnname || trimmedCnname.length > 20) {
        alert('昵称长度需大于 0 且小于或等于 20 位');
        return;
    }

    // 3. 校验密码
    // 要求：长度 >= 6 且 <= 20，只允许英文字母和数字
    const passwordPattern = /^[A-Za-z0-9]{6,20}$/;
    if (!passwordPattern.test(trimmedPassword)) {
        alert('密码格式错误：长度需在 6 到 20 位之间，且只能包含英文字母和数字');
        return;
    }

    // 4. 校验确认密码
    if (trimmedPassword !== confirmPassword.value.trim()) {
        alert('两次输入的密码不一致');
        return;
    }

    // 5. 执行注册请求
    try {
        const data = await fetch(`http://${ip}/user/register`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                username: trimmedUsername,
                password: trimmedPassword,
                cnname: trimmedCnname
            })
        });

        const json = await data.json();

        if (json.code === '0') {
            alert('注册成功！请告知管理员进行激活');
            router.push('/login');
        } else {
            alert('注册失败：' + (json.message || '未知错误'));
        }
    } catch (e) {
        alert('请求失败：' + (e.response?.json?.error || e.message || '网络错误'));
    }
};
</script>
<style scoped>
/* 1. 整体容器：居中、简洁背景 */
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  /* 移除强烈的渐变，使用柔和的浅灰色背景 */
  background-color: #f7f9fc; 
  font-family: "Helvetica Neue", Helvetica, Arial, "Microsoft YaHei", sans-serif;
}

/* 2. 卡片样式：更简洁、圆角、轻微阴影 */
.register-card {
  width: 380px;
  padding: 40px;
  background: #ffffff;
  border-radius: 12px; /* 适度圆角 */
  /* 轻微的盒子阴影，提供浮动感，但不突兀 */
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08); 
  border: 1px solid #e0e0e0; /* 增加细微边框 */
  animation: fadeIn 0.6s ease;
}

.title {
  text-align: center;
  margin-bottom: 30px; /* 增加间距 */
  font-size: 24px;
  color: #333333;
  font-weight: 600;
}

.form-group {
  margin-bottom: 20px;
}

label {
  display: block;
  margin-bottom: 8px;
  font-size: 14px;
  color: #4a4a4a;
  font-weight: 500;
}

input {
  width: 100%;
  padding: 12px 14px;
  border: 1px solid #cccccc;
  border-radius: 8px; /* 保持圆角 */
  box-sizing: border-box;
  font-size: 16px;
  color: #333;
  outline: none;
  transition: border-color 0.2s, box-shadow 0.2s;
}

input:focus {
  /* 使用一个现代的品牌色作为焦点色 */
  border-color: #007bff; 
  box-shadow: 0 0 0 3px rgba(0, 123, 255, 0.25);
}

.submit-btn {
  margin-top: 30px;
  width: 100%;
  padding: 12px;
  border: none;
  border-radius: 8px;
  /* 使用单一的品牌色，更专业 */
  background-color: #007bff; 
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.2s ease, transform 0.2s ease;
}

.submit-btn:hover {
  background-color: #0056b3;
  transform: translateY(-1px); /* 轻微上浮，更自然的交互效果 */
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.back-btn {
  margin-top: 15px; /* 稍微拉开与注册按钮的距离 */
  width: 100%;
  padding: 10px;
  border: none;
  border-radius: 8px;
  /* 次要按钮样式 */
  background-color: transparent; 
  color: #007bff; /* 使用品牌色，但无背景 */
  font-size: 14px;
  cursor: pointer;
  transition: background-color 0.2s ease, color 0.2s ease;
}

.back-btn:hover {
  /* 鼠标悬停时稍微改变背景颜色，提供反馈 */
  background-color: #e9f5ff; 
  color: #0056b3;
}
</style>