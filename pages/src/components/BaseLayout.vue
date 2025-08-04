<template>
  <el-container class="h-full">
    <el-header>
      <div class="w-full h-full flex items-center backdrop-blur">
        <div class="flex-1">
          <el-link href="/" :underline="false" class="text-6 styled-font">LetucOJ</el-link>
          <slot name="top-bar"></slot>
        </div>
        <div class="buttons">
          <slot name="top-right-bar">
            <el-button type="primary" plain @click="$router.push('/register')">注册</el-button>
            <el-button type="success" plain @click="$router.push('/login')">登录</el-button>
            <el-button type="warning" circle @click="$router.push('/docs')">
              <el-icon class="el-icon">
                <Document />
              </el-icon>
            </el-button>
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

</script>

<style>
header.el-header {
  box-shadow: var(--el-box-shadow);
  background-color: #fff1;
}
</style>
