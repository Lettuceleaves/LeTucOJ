<template>
  <el-container class="flex h-screen items-center justify-center">
    <el-card class="register-card">
      <div class="flex flex-row min-w-2xl min-h-sm">
        <el-space class="form-section">
          <el-text class="styled-font text-10 w-full text-center">LetucOJ</el-text>
          <el-form ref="formRef" :model="form" :rules="rules" label-position="top" label-width="auto" size="large">
            <el-form-item label="用户名" prop="username">
              <el-input v-model="form.username" placeholder="用户名" />
            </el-form-item>
            <el-form-item label="密码" prop="password">
              <el-input type="password" v-model="form.password" placeholder="密码" />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input type="password" v-model="form.confirmPassword" placeholder="确认密码" />
            </el-form-item>
            <el-form-item class="mt-4 mb-0">
              <el-button class="w-full" type="primary" @click="register">注册</el-button>
            </el-form-item>
          </el-form>
        </el-space>

        <el-space class="nav-section" direction="vertical" :size="8">
          <el-text class="styled-font text-10">欢迎</el-text>
          <el-text class="text-5">已有账户?</el-text>
          <el-button class="mt-2" type="primary" @click="router.push('/login')">
            <el-icon class="el-icon--left">
              <ArrowLeft />
            </el-icon>登录
          </el-button>
        </el-space>
      </div>

    </el-card>
  </el-container>
</template>

<script lang="ts" setup>
import { reactive, getCurrentInstance, ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import type { FormInstance, FormRules } from 'element-plus'

const router = useRouter()

const formRef = ref<FormInstance>()

const form = reactive({
  username: '',
  password: '',
  confirmPassword: ''
})
const rules = reactive<FormRules>({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 5, max: 20, message: '用户名长度在 5 到 20 个字符之间', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '用户名只能包含字母、数字和下划线', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 8, max: 20, message: '密码长度在 8 到 20 个字符之间', trigger: 'blur' },
    { pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^a-zA-Z0-9]).{8,20}$/, message: '密码必须包含大写字母、小写字母、数字和特殊字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== form.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      }, trigger: 'blur'
    }
  ]
})

const instance = getCurrentInstance()
const ip = instance.appContext.config.globalProperties.$ip

// 注册方法
const register = async () => {
  if (!await formRef.value.validate()) return
  try {
    const response = await axios.post(`http://${ip}:7777/user/register`, {
      username: form.username,
      password: form.password
    })

    const res = response.data

    if (res.status === 0) {
      router.push('/login')
    } else {
      alert('注册失败：' + (res.error || '未知错误'))
    }
  } catch (error) {
    alert('请求失败：' + (error.response?.data?.error || error.message || '网络错误'))
  }
}
</script>

<style scoped>
.register-card {
  border-radius: var(--el-border-radius-round);
  box-shadow: var(--el-box-shadow);
}

:deep(.el-form-item__label) {
  display: none !important;
}

.nav-section {
  flex-grow: 1;
  margin: -20px -20px -20px 20px;
  border-radius: 100px 0 0 100px;
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
