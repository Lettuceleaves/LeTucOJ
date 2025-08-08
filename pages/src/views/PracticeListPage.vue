<template>
  <main-layout selected-tab="practices">
    <el-container class="w-screen-lg ma flex flex-col !items-stretch gap-6 mt-8">
      <el-card>
        <el-input class="flex-1" placeholder="搜索" v-model="searchKeywords" clearable @keydown="handleSearch" @clear="handleSearchClear">
          <template #append>
            <el-button>
              <el-icon><Search /></el-icon>
            </el-button>
          </template>
        </el-input>
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

        <el-table v-if="!isFetching" :data="displayPractices" stripe @row-dblclick="row => handleItemClick(row.name, 'detail')">
          <el-table-column label="名称" prop="cnname" />
          <el-table-column label="操作" fixed="right" width="150">
            <template #default="scope">
              <el-button link type="primary" @click="handleItemClick(scope.$index, 'detail')">
                详情
              </el-button>
              <el-button link type="primary" @click="handleItemClick(scope.$index, 'submit')">
                提交
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <el-card v-if="displayMode === 'search'">
        <el-button-group>
          <el-button @click="handlePageChange(currentPage - 1)" :disabled="currentPage <= 1">Previous</el-button>
          <el-button @click="handlePageChange(currentPage + 1)" :disabled="displayPractices.length < LIMIT">Next</el-button>
        </el-button-group>
      </el-card>

      <el-card v-if="total > LIMIT && displayMode === 'all'">
        <el-pagination background layout="prev, pager, next" :page-size="LIMIT" :total="total"
          v-model:current-page="currentPage" @update:current-page="handlePageChange" />
      </el-card>
    </el-container>
  </main-layout>
</template>

<script lang="ts" setup>
import MainLayout from '@/components/MainLayout.vue';
import { GetPracticeListRequest, GetPracticesCountRequest, SearchPracticeRequest } from '@/apis/Practice';
import type { SimplePracticeInfo } from '@/models/Practice';
import { onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';

const route = useRoute();
const router = useRouter();
const LIMIT = 10;

const isFetching = ref(true);
const total = ref(0);
const searchKeywords = ref<string>();
const displayMode = ref<'all' | 'search'>('all');
const currentPage = ref(0);
const displayPractices = ref<SimplePracticeInfo[]>([]);

const fetchPractices = async (start: number, limit: number = LIMIT) => {
  isFetching.value = true;
  let response = await new GetPracticeListRequest(
    start,
    limit
  ).request();

  if (response.status === 0) {
    displayPractices.value = response.data!;
    displayMode.value = 'all';
  } else {
    console.error('Failed to fetch practices:', response.error);
  }
  isFetching.value = false;
}

const searchPractices = async (start: number, limit: number = LIMIT) => {
  if (!searchKeywords.value) return

  isFetching.value = true;
  let response = await new SearchPracticeRequest(
    searchKeywords.value,
    start,
    limit
  ).request();

  if (response.status === 0) {
    displayPractices.value = response.data!;
    displayMode.value = 'search';
  } else {
    console.error('Failed to fetch practices:', response.error);
    searchKeywords.value = undefined;
  }
  isFetching.value = false;
}

const handleSearch = async (event: Event | KeyboardEvent) => {
  if (!(event instanceof KeyboardEvent)) return

  let keyboardEvent = event as KeyboardEvent;
  if (keyboardEvent.code === 'Enter' && searchKeywords.value && searchKeywords.value !== route.query.q) {
    router.push({
      name: 'practices',
      query: { page: 1, q: searchKeywords.value }
    })
  }
}

const handleSearchClear = async () => {
  searchKeywords.value = undefined;
  handlePageChange(currentPage.value);
}

const handleItemClick = (row: number | string, operation: 'detail' | 'submit') => {
  const clickPracticeName = typeof row === 'number' ? displayPractices.value[row].name : row;

  router.push({
    name: operation,
    params: { name: clickPracticeName }
  });
}

const handlePageChange = (val: number) => {
  router.replace({
    name: 'practices',
    query: { page: val, q: searchKeywords.value }
  });
}

watch(() => route.query, async () => {
  let search: string | undefined;
  if (route.query.q) {
    search = route.query.q as string;
  }

  const page = Number(route.query.page) || 1;
  const start = (page - 1) * LIMIT;
  if (start < 0 || (!search && total.value <= start))
    router.push({
      name: 'practices',
      query: { page: 1 }
    });

  currentPage.value = page;
  searchKeywords.value = search;
  if (search) await searchPractices(start);
  else await fetchPractices(start);
}, { immediate: true });

onMounted(async () => {
  let response = await new GetPracticesCountRequest().request();
  if (response.status !== 0) return;
  total.value = response.data!;
});
</script>

<style scoped></style>
