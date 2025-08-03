<script setup>
import { ArrowRight, Document } from '@element-plus/icons-vue';
import { ref, onMounted } from 'vue';

const lines = ref('');

onMounted(async () => {
  try {
    const response = await fetch('/code.txt')
    if (!response.ok) throw new Error('Failed to fetch data');
    lines.value = await response.text();
  } catch (error) {
    console.error('Error fetching lines:', error);
    lines.value = "rasie Error('Failed to load lines. Please check the URL and try again.')";
  }
});
</script>

<template>
  <div class="home-view-layout">
    <div class="bg-terminal" style="--t:6s">
      <div>
        <highlightjs :code="lines" language="python"></highlightjs>
      </div>
      <div>
        <highlightjs :code="lines" language="python"></highlightjs>
      </div>
    </div>

    <el-container style="height: 100%;">
      <el-header>
        <div class="header-bar">
          <el-text style="font-size: x-large;">LetucOJ</el-text>
          <div class="buttons">
            <el-button type="primary" plain @click="$router.push('/register')">注册</el-button>
            <el-button type="success" plain @click="$router.push('/login')">登录</el-button>
            <el-button type="warning" circle @click="$router.push('/docs')" :icon="Document"></el-button>
          </div>
        </div>
      </el-header>

      <el-main style="height: 100%;">
        <div class="title">
          <el-text style="font-size: 4rem">LetucOJ</el-text>
          <el-text style="font-size: 2.5rem; margin: 32px 0; color: #ccc;">
            一个试图让出题变简单的 OJ 系统
          </el-text>

          <el-button round @click="$router.push('/login')" size="large"
            style="font-size: 1.5rem; padding: 32px;">
            启动!<el-icon class="el-icon--right"><ArrowRight /></el-icon>
          </el-button>
        </div>
      </el-main>
    </el-container>
  </div>
</template>

<style scoped>
.home-view-layout {
  height: 100vh;
  background-color: #282c34;
}

.bg-terminal {
  position: absolute;
  top: 0;
  left: 0px;
  width: 100%;
  height: 100%;
  pointer-events: none;
  overflow: hidden;
  z-index: 0;

  mask-image: linear-gradient(
    0deg, transparent, #fff 5%, #fff 85%, transparent
  );
}

.bg-terminal div {
  animation: animate1 var(--t) linear infinite;
}

.bg-terminal div:nth-child(2) {
  animation: animate2 var(--t) linear infinite;
  animation-delay: calc(var(--t) / -2);
}

@keyframes animate1 {
  from {
    transform: translateY(100%);
  }
  to {
    transform: translateY(-100%);
  }
}
@keyframes animate2 {
  from {
    transform: translateY(0);
  }
  to {
    transform: translateY(-200%);
  }
}

::v-deep(pre) code.hljs {
  font-size: 2rem;
}

.home-view-layout .el-container {
  position: relative;
  z-index: 10;
  background-color: #00000066;
}

header.el-header {
  box-shadow: var(--el-box-shadow);
  backdrop-filter: blur(10px);
  background-color: #fff1;
}

.header-bar {
  height: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

div.title {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #fff;
}

</style>
