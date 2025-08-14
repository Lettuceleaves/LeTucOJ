<template>
  <div class="contest-parent">
    <!-- 标题栏 -->
    <div class="title-bar">
      <div class="title-item" :class="{ active: activeTab === 'list' }"
           @click="activeTab = 'list'">题单</div>
      <div class="title-item" :class="{ active: activeTab === 'contest' }"
           @click="activeTab = 'contest'">竞赛</div>
      <div class="title-item" :class="{ active: activeTab === 'user' }"
           @click="activeTab = 'user'">用户</div>
      <div class="spacer"></div>
    </div>

    <!-- 内容区域 -->
    <div class="content">
      <ProblemList v-show="activeTab === 'list'" />
      <Contest     v-show="activeTab === 'contest'" />
      <User        v-show="activeTab === 'user'" />  <!-- 2. 直接用 v-show -->
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import ProblemList from './MainPages/List.vue'
import Contest from './MainPages/Contest.vue'
import User from '../views/UserPage.vue'

const activeTab = ref('list')
const listRef = ref(null)
const contestRef = ref(null)
const router = useRouter()

const onCreate = () => {
  router.push('/form')
}

const onUser = () => {
  router.push('/user')
}

const onHistory = () => {
  router.push('/history')
}

const onCompetition = () => {
  router.push('/competition')
}
</script>

<style scoped>
.contest-parent {
  height: 100vh;
  display: flex;
  flex-direction: column;
  font-family: system-ui, -apple-system, BlinkMacSystemFont, sans-serif;
}
.title-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  background: #f5f5f5;
  border-bottom: 1px solid #d1d5e0;
}
.title-item {
  padding: 10px 18px;
  cursor: pointer;
  font-weight: 600;
  border-radius: 6px;
  transition: background 0.2s;
}
.title-item.active {
  background: #3b82f6;
  color: white;
}
.spacer {
  flex: 1;
}
.content {
  flex: 1;
  position: relative;
  overflow: auto;
  padding: 0;
}
.placeholder {
  padding: 40px;
  color: #555;
  font-size: 1rem;
}
</style>
