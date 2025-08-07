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
      <el-card>
        <el-descriptions :title="practiceDetail.cnname" :column="2" label-width="100">
          <template #extra>
            <el-button type="primary">操作</el-button>
          </template>

          <el-descriptions-item label="题目 ID">{{ practiceDetail.name }}</el-descriptions-item>
          <el-descriptions-item label="作者">{{ practiceDetail.authors }}</el-descriptions-item>
          <el-descriptions-item label="频率(总次数)">{{ practiceDetail.freq }}</el-descriptions-item>
          <el-descriptions-item label="标签">
            <span class="inline-flex gap-2">
              <el-tag v-for="(tag, index) in practiceDetail.tags.split(',')" v-bind:key="index">{{ tag }}</el-tag>
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ practiceDetail.createtime }}</el-descriptions-item>
          <el-descriptions-item label="更新时间">{{ practiceDetail.updateat }}</el-descriptions-item>

          <el-descriptions-item label="状态" v-if="userInfo?.role !== 'USER'">
            <span>
              <el-tag type="success" v-if="practiceDetail.ispublic">公开</el-tag>
              <el-tag type="warning" v-if="!practiceDetail.ispublic">隐藏</el-tag>
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="难度">
            <span class="relative top-1">
              <el-rate v-model="practiceDetail.difficulty" :colors="difficultyColors" size="large" allow-half
                disabled />
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="测试点数量">{{ practiceDetail.caseAmount }}</el-descriptions-item>
        </el-descriptions>
      </el-card>

      <el-card>
        <div v-html="contentMarkdown"></div>
      </el-card>
    </el-container>
  </main-layout>
</template>

<script lang="ts" setup>
import { GetPracticeDetailRequest } from '@/apis/Practice';
import MainLayout from '@/components/MainLayout.vue';
import type { PraciceInfo } from '@/models/Practice';
import { getDecodedJwt } from '@/persistence/LocalPersistence';
import { marked } from 'marked';
import { computed, onMounted, ref } from 'vue';
import { useRoute } from 'vue-router';

const route = useRoute();

const practiceName = route.params.name as string;

const practiceDetail = ref<PraciceInfo>();
const userInfo = ref(getDecodedJwt());
const difficultyColors = ref(['#99A9BF', '#F7BA2A', '#FF9900']);

const contentMarkdown = computed(() => {
  if (!practiceDetail.value) return;
  return marked(practiceDetail.value.content);
});

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
