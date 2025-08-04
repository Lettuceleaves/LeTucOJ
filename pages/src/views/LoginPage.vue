<template>
  <el-container class="flex h-screen items-center justify-center">
    <el-card class="login-card">
      <div class="flex flex-row min-w-2xl min-h-sm">
        <el-space class="nav-section" direction="vertical" :size="8">
          <el-text class="styled-font text-10">欢迎</el-text>
          <el-text class="text-5">没有账户?</el-text>
          <el-button class="mt-2" type="primary" @click="router.push('/register')">
            注册<el-icon class="el-icon--right"><ArrowRight /></el-icon>
          </el-button>
        </el-space>

        <el-space class="form-section">
          <el-text class="styled-font text-10 w-full text-center">LetucOJ</el-text>
          <el-form ref="formRef" :model="form" :rules="rules" label-position="top" label-width="auto" size="large">
            <el-form-item label="用户名" prop="username">
              <el-input v-model="form.username" placeholder="用户名" />
            </el-form-item>
            <el-form-item label="密码" prop="password">
              <el-input type="password" v-model="form.password" placeholder="密码" />
            </el-form-item>
            <el-form-item>
              <el-button class="w-full mt-4" type="primary" @click="login">登录</el-button>
            </el-form-item>
          </el-form>
        </el-space>
      </div>

    </el-card>
  </el-container>
</template>

<script lang="ts" setup>
import { reactive, getCurrentInstance, ref } from 'vue';
import axios from 'axios';
import { useRouter } from 'vue-router';
import type { FormInstance, FormRules } from 'element-plus';

const formRef = ref<FormInstance>()
const form = reactive({
  username: '',
  password: ''
})
const rules = reactive<FormRules>({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
})

const router = useRouter();

const instance = getCurrentInstance()
const ip = instance.appContext.config.globalProperties.$ip

const login = async () => {
  if (! await formRef.value.validate()) return

  try {
    const token = localStorage.getItem('jwt')
    const response = await axios.post(`http://${ip}:7777/user/login`, {
      username: form.username,
      password: form.password,
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
.login-card {
  border-radius: var(--el-border-radius-round);
  box-shadow: var(--el-box-shadow);
}

:deep(.el-form-item__label) {
  display: none !important;
}

.nav-section {
  flex-grow: 1;
  margin: -20px 20px -20px -20px;
  border-radius: 0 100px 100px 0;
  padding: 20px;
  background-color: var(--el-color-primary);
  justify-content: center;
}

.form-section {
  flex-grow: 1;
  margin: 20px;
  display: flex;
  flex-direction: column;
  justify-content: space-around;
  align-items: stretch !important;
}
</style>
