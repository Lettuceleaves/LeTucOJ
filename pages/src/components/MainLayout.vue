<template>
  <base-layout>
    <template #top-bar>
      <el-button
        @click="onTabClicked('contest')"
        :text="selectedTab !== 'contest'"
        :type="selectedTab === 'contest' ? 'primary' : ''"
        :bg="selectedTab === 'contest'">竞赛</el-button>

      <el-button
        @click="onTabClicked('practices')"
        :text="selectedTab !== 'practices'"
        :type="selectedTab === 'practices' ? 'primary' : ''"
        :bg="selectedTab === 'practices'">题单</el-button>

      <el-button
        @click="onTabClicked('detail')"
        :text="selectedTab !== 'detail'"
        :type="selectedTab === 'detail' ? 'primary' : ''"
        :bg="selectedTab === 'detail'">详情</el-button>

      <el-button
        @click="onTabClicked('management')"
        :text="selectedTab !== 'management'"
        :type="selectedTab === 'management' ? 'primary' : ''"
        :bg="selectedTab === 'management'">管理</el-button>
    </template>

    <slot></slot>
  </base-layout>
</template>

<script lang="ts" setup>
import BaseLayout from '@/components/BaseLayout.vue'

const { selectedTab } = defineProps<{
  selectedTab: 'contest' | 'practices' | 'detail' | 'management'
}>();

const onTabClicked = (tab: typeof selectedTab) => {

}
</script>

<style scoped></style>
