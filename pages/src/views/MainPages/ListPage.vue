<template>
  <el-container class="w-screen-lg ma flex flex-col !items-stretch">
    <el-card class="my-2">
      <el-input placeholder="搜索一下"></el-input>
    </el-card>

    <el-card class="my-2">
      <el-skeleton animated>
        <template #template>
          <el-table :data="Array.from({ length: 3 })">
            <el-table-column label="名称">
              <template #default>
                <el-skeleton :rows="1" />
              </template>
            </el-table-column>
            <el-table-column label="操作" fixed="right">
              <template #default>
                <el-skeleton :rows="1" />
              </template>
            </el-table-column>
          </el-table>
        </template>
      </el-skeleton>

      <el-table v-if="!isFetching" :data="displayPractices">
        <el-table-column prop="cnname" label="名称" />
        <el-table-column fixed="right" label="操作">
          <template #default>
            <el-button link type="primary">
              查看
            </el-button>
          </template>
        </el-table-column>
        <!-- <el-table-column prop="createdAt" label="创建时间" />
        <el-table-column prop="updatedAt" label="更新时间" /> -->
      </el-table>
    </el-card>

    <el-card class="my-2" v-if="total > 10">
      <el-pagination background layout="prev, pager, next" :page-size="10" :total="total" />
    </el-card>
  </el-container>
</template>

<script lang="ts" setup>
import { GetPracticeListRequest } from '@/apis/Practice';
import type { SimplePracticeInfo } from '@/models/Practice';
import { onMounted, ref } from 'vue';

const isFetching = ref(true);
const start = ref(0);
const total = ref<number>(0);
const displayPractices = ref<SimplePracticeInfo[]>([]);

// Fetch practices
const fetchPractices = async (start: number, limit: number = 10) => {
  isFetching.value = true;
  let response = await new GetPracticeListRequest(
    start,
    limit,
    "",
    ""
  ).request();

  if (response.status === 0) {
    displayPractices.value = response.data!;
  } else {
    console.error('Failed to fetch practices:', response.error);
  }
  isFetching.value = false;
}

onMounted(async () => {
  // TODO:
  // let response = await get<GetPracticesCountRequest>('/practice/count');
  // if (response.status !== 0) return;
  // total.value = response.data!;

  total.value = 50;

  await fetchPractices(start.value);

});
</script>

<style scoped></style>
