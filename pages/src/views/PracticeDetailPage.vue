<template>
  <main-layout selected-tab="detail">
    <el-container v-if="!practiceDetail" class="w-screen-lg ma flex flex-col !items-stretch gap-4">
      <el-card>
        <el-skeleton :rows="6" animated />
      </el-card>

      <el-card>
        <el-skeleton :rows="3" animated />
      </el-card>
    </el-container>

    <el-container v-if="practiceDetail" class="w-screen-lg ma flex flex-col !items-stretch gap-4">
      <practice-info-card :practice-detail="practiceDetail" />

      <el-card>
        <markdown-section :md-text="practiceDetail.content" />
      </el-card>
    </el-container>
  </main-layout>
</template>

<script lang="ts" setup>
import { GetPracticeDetailRequest } from '@/apis/Practice';
import MainLayout from '@/components/MainLayout.vue';
import PracticeInfoCard from '@/components/PracticeInfoCard.vue';
import MarkdownSection from '@/components/MarkdownSection.vue';
import type { PraciceInfo } from '@/models/Practice';
import { onMounted, ref } from 'vue';
import { useRoute } from 'vue-router';

const route = useRoute();

const practiceName = route.params.name as string;
const practiceDetail = ref<PraciceInfo>();

onMounted(async () => {
  let resp = await new GetPracticeDetailRequest(practiceName).request();
  if (resp.status !== 0) {
    console.log(resp.error);
    return
  }

  practiceDetail.value = resp.data!;
});

</script>

<style scoped>
:deep(.el-rate) {
  height: 100%;
}
</style>
