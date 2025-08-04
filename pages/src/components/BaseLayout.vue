<template>
  <el-container class="h-full">
    <el-header>
      <div class="w-full h-full flex items-center backdrop-blur">
        <div class="flex-1">
          <el-link href="/" underline="never" class="text-6 styled-font">LetucOJ</el-link>
          <slot name="top-bar"></slot>
        </div>

        <slot name="top-right-bar">
          <div v-if="jwtPayload">
            <el-dropdown trigger="click" class="mr-4">
              <span class="flex items-baseline">
                <el-avatar class="mr-2"></el-avatar>
                <el-text class="text-6 mb-1">{{ jwtPayload.username }}</el-text>
                <i class="el-icon-arrow-down el-icon--right"></i>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="$router.push('/profile')">个人中心</el-dropdown-item>
                  <el-dropdown-item @click="$router.push('/settings')">设置</el-dropdown-item>
                  <el-dropdown-item divided @click="$router.push('/logout')">登出</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>

          <div v-if="!jwtPayload">
            <el-button type="primary" plain @click="$router.push('/register')">注册</el-button>
            <el-button type="success" plain @click="$router.push('/login')">登录</el-button>
            <el-button type="warning" circle @click="$router.push('/docs')">
              <el-icon class="el-icon">
                <Document />
              </el-icon>
            </el-button>
          </div>
        </slot>
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
import { getJwt, type JwtPayload } from '@/persistence/LocalPersistence';
import { ref } from 'vue';

const jwtPayload = ref<JwtPayload | undefined>(getJwt());
</script>

<style>
header.el-header {
  box-shadow: var(--el-box-shadow);
  background-color: #fff1;
}
</style>
