<template>
  <el-container class="w-screen-lg ma">
    <el-card class="w-full">
      <el-input placeholder="搜索一下"></el-input>
    </el-card>
  </el-container>
</template>

<script lang="ts" setup>
import type { SimplePracticeInfo } from '@/models/Practice';
import { ref } from 'vue';

const displayPractices = ref<SimplePracticeInfo[]>();
</script>

<style scoped>
</style>
