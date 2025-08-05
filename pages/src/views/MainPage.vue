<template>
  <base-layout>
    <template #top-bar>
      <el-button text>竞赛</el-button>
      <el-button text>题单</el-button>
      <el-button text>详情</el-button>
      <el-button text>管理</el-button>
    </template>

    <list-page />
  </base-layout>
</template>

<script lang="ts" setup>
import BaseLayout from '@/components/BaseLayout.vue'
import ListPage from './MainPages/ListPage.vue'
</script>

<style scoped>
</style>
