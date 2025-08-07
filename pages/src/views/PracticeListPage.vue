<template>
  <main-layout selected-tab="practices">
    <el-container class="w-screen-lg ma flex flex-col !items-stretch gap-4">
      <el-card>
        <el-input placeholder="搜索一下"></el-input>
      </el-card>

      <el-card>
        <el-skeleton animated v-if="isFetching">
          <template #template>
            <el-table :data="Array.from({ length: 3 })">
              <el-table-column label="名称">
                <template #default>
                  <el-skeleton :rows="1" />
                </template>
              </el-table-column>
              <el-table-column label="操作" fixed="right" width="150">
                <template #default>
                  <el-skeleton :rows="1" />
                </template>
              </el-table-column>
            </el-table>
          </template>
        </el-skeleton>

        <el-table v-if="!isFetching" :data="displayPractices" stripe>
          <el-table-column label="名称" prop="cnname" />
          <el-table-column label="操作" fixed="right" width="150">
            <template #default="scope">
              <el-button link type="primary" @click="handleViewPractice(scope.$index)">
                查看
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <el-card v-if="total > LIMIT">
        <el-pagination background layout="prev, pager, next" :page-size="LIMIT" :total="total"
          v-model:current-page="currentPage" @update:current-page="handlePageChange" />
      </el-card>
    </el-container>
  </main-layout>
</template>

<script lang="ts" setup>
import MainLayout from '@/components/MainLayout.vue';
import { GetPracticeListRequest, GetPracticesCountRequest } from '@/apis/Practice';
import type { SimplePracticeInfo } from '@/models/Practice';
import { onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';

const route = useRoute();
const router = useRouter();
const LIMIT = 10;

const isFetching = ref(true);
const total = ref(0);
const currentPage = ref(0);
const displayPractices = ref<SimplePracticeInfo[]>([]);

// Fetch practices
const fetchPractices = async (start: number, limit: number = LIMIT) => {
  isFetching.value = true;
  let response = await new GetPracticeListRequest(
    start,
    limit
  ).request();

  if (response.status === 0) {
    displayPractices.value = response.data!;
  } else {
    console.error('Failed to fetch practices:', response.error);
  }
  isFetching.value = false;
}

const handleViewPractice = (index: number) => {
  const practice = displayPractices.value[index];

  router.push({
    name: 'detail',
    params: { name: practice.name }
  });
}

const handlePageChange = (val: number) => {
  router.push({
    name: 'practices',
    query: { page: val }
  });
}

watch(() => route.query, async () => {
  const page = Number(route.query.page) || 1;
  const start = (page - 1) * LIMIT;
  if (start < 0 || total.value <= start)
    router.push({
      name: 'practices',
      query: { page: 1 }
    });

  currentPage.value = page;
  await fetchPractices(start);
}, { immediate: true });

onMounted(async () => {
  let response = await new GetPracticesCountRequest().request();
  if (response.status !== 0) return;
  total.value = response.data!;
});
</script>

<style scoped></style>
