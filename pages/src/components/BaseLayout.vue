<template>
  <el-container class="h-full">
    <el-header>
      <div class="w-full h-full flex items-center backdrop-blur">
        <div class="flex-1">
          <el-link href="/" underline="never" class="text-6 mx-4 styled-font">LetucOJ</el-link>
          <slot name="top-bar"></slot>
        </div>

        <div class="flex flex-items-center gap-2 mx-4">
          <slot name="top-right-bar">
            <el-dropdown v-if="userInfo !== null" trigger="click">
              <span class="flex flex-items-center gap-2">
                <el-avatar></el-avatar>

                <div class="flex flex-col mb-1">
                  <el-text class="text-6">{{ userInfo.cnname }}</el-text>

                  <el-text class="text-3 w-full" v-if="userInfo.role === 'USER'" type="info">用户</el-text>
                  <el-text class="text-3 w-full" v-if="userInfo.role === 'MANAGER'" type="primary">管理员</el-text>
                  <el-text class="text-3 w-full" v-if="userInfo.role === 'ROOT'" type="danger">ROOT</el-text>
                </div>

                <el-icon>
                  <ArrowDown />
                </el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="router.push('/profile')">个人中心</el-dropdown-item>
                  <el-dropdown-item @click="router.push('/settings')">设置</el-dropdown-item>
                  <el-dropdown-item divided @click="logout">登出</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>


            <div v-if="userInfo === null">
              <el-button type="primary" plain @click="router.push({ name: 'register' })">注册</el-button>
              <el-button type="success" plain @click="router.push({ name: 'login' })">登录</el-button>
              <el-button type="warning" circle @click="router.push({ name: 'docs' })">
                <el-icon>
                  <Document />
                </el-icon>
              </el-button>
            </div>
          </slot>
        </div>
      </div>
    </el-header>

    <el-main class="h-full">
      <slot name="main">
        <slot></slot>
      </slot>
    </el-main>
  </el-container>
</template>

<script lang="ts" setup>
import type { UserInfo } from '@/models/User';
import { clearJwt, getDecodedJwt } from '@/persistence/LocalPersistence';
import { ref } from 'vue';
import { useRouter } from 'vue-router';

const userInfo = ref<UserInfo | null>(getDecodedJwt());

const router = useRouter();

const logout = () => {
  clearJwt();
  userInfo.value = null;

  router.push({ name: 'home' });
}
</script>

<style>
header.el-header {
  box-shadow: var(--el-box-shadow);
  background-color: #fff1;
}
</style>
