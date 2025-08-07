<template>
  <div class="h-screen">
    <el-image src="https://t.alcy.cc/fj" v-if="isImgBgEnabled" fit="fill" class="absolute z-0 h-full w-full" />

    <base-layout class="relative z-10">
      <template #top-bar>
        <el-button @click="onTabClicked('contest')" :text="selectedTab !== 'contest'"
          :type="selectedTab === 'contest' ? 'primary' : ''" :bg="selectedTab === 'contest'">竞赛</el-button>

        <el-button @click="onTabClicked('practices')" :text="selectedTab !== 'practices'"
          :type="selectedTab === 'practices' ? 'primary' : ''" :bg="selectedTab === 'practices'">题单</el-button>

        <el-button @click="onTabClicked('detail')" :text="selectedTab !== 'detail'"
          :type="selectedTab === 'detail' ? 'primary' : ''" :bg="selectedTab === 'detail'">详情</el-button>

        <el-button @click="onTabClicked('management')" :text="selectedTab !== 'management'"
          :type="selectedTab === 'management' ? 'primary' : ''" :bg="selectedTab === 'management'">管理</el-button>
      </template>

      <template #top-right-bar>
        <el-switch v-model="isImgBgEnabled" class="mr-4">
          <span>背景图片</span>
        </el-switch>
      </template>

      <slot></slot>
    </base-layout>
  </div>
</template>

<script lang="ts" setup>
import BaseLayout from '@/components/BaseLayout.vue'
import { ref } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();

const isImgBgEnabled = ref(false);

const { selectedTab } = defineProps<{
  selectedTab: 'contest' | 'practices' | 'detail' | 'management'
}>();

const onTabClicked = (tab: typeof selectedTab) => {
  if (tab === 'contest') {
    // TODO
  } else if (tab === 'practices') {
    router.push({ name: 'practices' });
  } else if (tab === 'detail') {
    // TODO
  } else if (tab === 'management') {
    // TODO
  }
}
</script>

<style scoped></style>
