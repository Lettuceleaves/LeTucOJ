<template>
  <main-layout selected-tab="detail">
    <el-container v-if="!practiceDetail" class="w-screen-lg ma flex flex-col !items-stretch gap-6 mt-8">
      <el-card>
        <el-skeleton :rows="6" animated />
      </el-card>

      <el-card>
        <el-skeleton :rows="3" animated />
      </el-card>
    </el-container>

    <el-container v-if="practiceDetail" class="w-screen-lg ma flex flex-col !items-stretch gap-6 mt-8">
      <el-card>
        <el-page-header @back="router.back()">
          <template #content>
            <div class="flex items-baseline">
              <span class="text-6 font-bold">{{ practiceDetail.cnname }}</span>
              <span class="text-4 ml-2 text-gray-500">#{{ practiceDetail.name }}</span>
            </div>
          </template>
          <template #extra>
            <el-button type="primary" @click="handleSubmitClick">
              <el-icon class="el-icon--left"><Plus /></el-icon>提交
            </el-button>

            <el-button type="warning" v-if="user?.role !== 'USER'">管理</el-button>
          </template>
          <template #default>
            <practice-description class="mt-4" :practice-detail="practiceDetail" />
          </template>
        </el-page-header>
      </el-card>

      <el-card>
        <markdown-section :md-text="practiceDetail.content" />
      </el-card>

      <el-card>
        TODO: 历史提交
      </el-card>
    </el-container>
  </main-layout>
</template>

<script lang="ts" setup>
import { GetPracticeDetailRequest } from '@/apis/Practice';
import MainLayout from '@/components/MainLayout.vue';
import PracticeDescription from '@/components/PracticeDescription.vue';
import MarkdownSection from '@/components/MarkdownSection.vue';
import type { PraciceInfo } from '@/models/Practice';
import { onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { getDecodedJwt } from '@/persistence/LocalPersistence';

const route = useRoute();
const router = useRouter();

const user = ref(getDecodedJwt());

const practiceName = route.params.name as string;
const practiceDetail = ref<PraciceInfo>();

const handleSubmitClick = () => {
  router.push({
    name: 'submit',
    params: { name: practiceName }
  });
}

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
