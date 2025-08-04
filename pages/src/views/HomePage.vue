<script lang="ts" setup>
import { ref, onMounted } from 'vue';
import BaseLayout from '@/components/BaseLayout.vue';

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
        <highlightjs class="text-8" :code="lines" language="python"></highlightjs>
      </div>
      <div>
        <highlightjs class="text-8" :code="lines" language="python"></highlightjs>
      </div>
    </div>

    <base-layout class="h-full relative z-10 bg-black bg-op-50">
      <div class="flex items-center justify-center flex-col h-full">
        <el-text class="text-16 styled-font">LetucOJ</el-text>
        <el-text class="text-9 my-4 c-gray">
          一个试图让出题变简单的 OJ 系统
        </el-text>

        <el-button round @click="$router.push('/login')" size="large" class="!p-8 my-4 text-6">
          启动!<el-icon class="el-icon--right">
            <ArrowRight />
          </el-icon>
        </el-button>
      </div>
    </base-layout>
  </div>
</template>

<style lang="ts" scoped>
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

  mask-image: linear-gradient(0deg, transparent, #fff 5%, #fff 85%, transparent);
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
</style>
