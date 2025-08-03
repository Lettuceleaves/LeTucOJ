<template>
  <div class="p-4 space-y-4">
    <div class="flex flex-wrap gap-4 items-center">
      <div class="flex gap-2">
        <label class="font-medium">用户（中文名/用户名）:</label>
        <input v-model="filter.cnname" @keyup.enter="fetchAny" placeholder="输入用户名或中文名" class="border px-2 py-1 rounded" />
        <button @click="fetchAny" class="px-3 py-1 bg-blue-600 text-white rounded">查询指定用户</button>
      </div>
      <div>
        <button @click="fetchAll" class="px-3 py-1 bg-gray-700 text-white rounded">查看全部用户提交</button>
      </div>
      <div class="ml-auto flex gap-2">
        <input v-model="searchText" placeholder="全局搜索（题目/语言）" class="border px-2 py-1 rounded" />
        <button @click="applyLocalFilter" class="px-3 py-1 bg-teal-600 text-white rounded">筛选</button>
        <button @click="resetFilters" class="px-3 py-1 bg-gray-400 text-black rounded">重置</button>
      </div>
    </div>

    <!-- 列表 -->
    <div class="overflow-x-auto">
      <table class="min-w-full border-collapse">
        <thead>
          <tr class="bg-gray-100">
            <th class="p-2 text-left">用户名</th>
            <th class="p-2 text-left">中文名</th>
            <th class="p-2 text-left">题目</th>
            <th class="p-2 text-left">语言</th>
            <th class="p-2 text-left">结果</th>
            <th class="p-2 text-left">耗时</th>
            <th class="p-2 text-left">内存</th>
            <th class="p-2 text-left">提交时间</th>
            <th class="p-2 text-left">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(item, idx) in pagedData" :key="item.userName + '_' + item.submitTime + '_' + idx" class="border-b hover:bg-gray-50">
            <td class="p-2">{{ item.userName }}</td>
            <td class="p-2">{{ item.cnname }}</td>
            <td class="p-2">{{ item.problemName }}</td>
            <td class="p-2">{{ item.language }}</td>
            <td class="p-2">
              <span
                :class="[
                  'px-2 py-0.5 rounded text-sm font-medium',
                  resultClass(item.result)
                ]"
              >
                {{ item.result }}
              </span>
            </td>
            <td class="p-2">{{ formatTime(item.timeUsed) }}</td>
            <td class="p-2">{{ formatMemory(item.memoryUsed) }}</td>
            <td class="p-2">{{ formatDate(item.submitTime) }}</td>
            <td class="p-2">
              <button @click="openDetail(item)" class="text-blue-600 underline text-sm">详情</button>
            </td>
          </tr>
          <tr v-if="filtered.length === 0">
            <td class="p-4 text-center" colspan="9">暂无数据</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 分页 -->
    <div class="flex justify-between items-center mt-2">
      <div>
        共 {{ filtered.length }} 条，当前第 {{ currentPage }} / {{ pageCount }} 页
      </div>
      <div class="flex gap-2">
        <button :disabled="currentPage === 1" @click="currentPage--" class="px-2 py-1 border rounded">上一页</button>
        <button :disabled="currentPage === pageCount" @click="currentPage++" class="px-2 py-1 border rounded">下一页</button>
        <select v-model.number="pageSize" class="border px-2 py-1 rounded">
          <option :value="10">10/页</option>
          <option :value="25">25/页</option>
          <option :value="50">50/页</option>
        </select>
      </div>
    </div>

    <!-- 详情模态 -->
    <div v-if="showDetail" class="fixed inset-0 bg-black/30 flex items-center justify-center z-50">
      <div class="bg-white rounded-xl w-full max-w-3xl p-6 relative">
        <button @click="showDetail = false" class="absolute top-3 right-3 text-gray-600">✕</button>
        <h2 class="text-xl font-bold mb-2">提交详情</h2>
        <div class="grid grid-cols-2 gap-4">
          <div>
            <p><strong>用户：</strong>{{ detail.userName }} / {{ detail.cnname }}</p>
            <p><strong>题目：</strong>{{ detail.problemName }}</p>
            <p><strong>语言：</strong>{{ detail.language }}</p>
            <p><strong>提交时间：</strong>{{ formatDate(detail.submitTime) }}</p>
            <p><strong>耗时：</strong>{{ formatTime(detail.timeUsed) }}</p>
            <p><strong>内存：</strong>{{ formatMemory(detail.memoryUsed) }}</p>
            <p><strong>结果：</strong>
              <span :class="['px-2 py-0.5 rounded', resultClass(detail.result)]">{{ detail.result }}</span>
            </p>
          </div>
          <div>
            <p><strong>题目描述/备注（可扩展）</strong></p>
            <!-- 如果有额外字段可以显示 -->
          </div>
        </div>
        <div class="mt-4">
          <div class="mb-2"><strong>源码：</strong></div>
          <pre class="bg-gray-100 p-3 rounded overflow-auto max-h-60"><code>{{ detail.code }}</code></pre>
        </div>
        <div class="mt-4">
          <div class="mb-2"><strong>运行结果 / 输出：</strong></div>
          <pre class="bg-gray-100 p-3 rounded overflow-auto max-h-40"><code>{{ detail.result }}</code></pre>
        </div>
      </div>
    </div>

    <!-- 错误提示 -->
    <div v-if="error" class="text-red-600 mt-2">{{ error }}</div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import axios from 'axios';

// 全局身份参数，假设放在 header
const GLOBAL_AUTH = 'ABC';

interface RecordItem {
  userName: string;
  cnname: string;
  problemName: string;
  language: string;
  code: string;
  result: string;
  timeUsed: number;
  memoryUsed: number;
  submitTime: number;
}

const rawData = ref<RecordItem[]>([]);
const filtered = ref<RecordItem[]>([]);
const filter = ref({ cnname: '' });
const searchText = ref('');
const error = ref<string | null>(null);
const showDetail = ref(false);
const detail = ref<RecordItem | null>(null);

// 分页
const currentPage = ref(1);
const pageSize = ref(10);
const pageCount = computed(() => Math.max(1, Math.ceil(filtered.value.length / pageSize.value)));
const pagedData = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value;
  return filtered.value.slice(start, start + pageSize.value);
});

// 结果到 class 映射（可替换为你项目的 class）
function resultClass(result: string) {
  const lower = result.toLowerCase();
  if (lower.includes('accepted') || lower.includes('ac')) return 'bg-green-100 text-green-800';
  if (lower.includes('wrong')) return 'bg-red-100 text-red-800';
  if (lower.includes('time')) return 'bg-yellow-100 text-yellow-800';
  if (lower.includes('error')) return 'bg-purple-100 text-purple-800';
  if (lower.includes('pending') || lower.includes('compiling')) return 'bg-gray-100 text-gray-800';
  return 'bg-gray-50 text-gray-700';
}

// 格式化
function formatTime(ms: number) {
  return `${ms} ms`;
}
function formatMemory(bytes: number) {
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / 1024 / 1024).toFixed(1)} MB`;
}
function formatDate(ts: number) {
  const d = new Date(ts);
  return d.toLocaleString();
}

// 拉全部
async function fetchAll() {
  error.value = null;
  try {
    const res = await axios.post('/recordList/all', {}, {
      headers: {
        Authorization: GLOBAL_AUTH,
      },
    });
    if (res.data.status !== 0) {
      error.value = res.data.error || '后台返回非 0 状态';
      rawData.value = [];
    } else {
      rawData.value = res.data.data;
      applyLocalFilter();
      currentPage.value = 1;
    }
  } catch (e: any) {
    error.value = '请求失败：' + (e.message || e);
  }
}

// 指定用户
async function fetchAny() {
  error.value = null;
  try {
    const res = await axios.post('/recordList/any', { cnname: filter.value.cnname }, {
      headers: {
        Authorization: GLOBAL_AUTH,
      },
    });
    if (res.data.status !== 0) {
      error.value = res.data.error || '后台返回非 0 状态';
      rawData.value = [];
    } else {
      rawData.value = res.data.data;
      applyLocalFilter();
      currentPage.value = 1;
    }
  } catch (e: any) {
    error.value = '请求失败：' + (e.message || e);
  }
}

// 本地筛选（搜索题目/语言等）
function applyLocalFilter() {
  const keyword = searchText.value.trim().toLowerCase();
  if (!keyword) {
    filtered.value = [...rawData.value];
  } else {
    filtered.value = rawData.value.filter(i => {
      return (
        i.problemName.toLowerCase().includes(keyword) ||
        i.language.toLowerCase().includes(keyword) ||
        i.userName.toLowerCase().includes(keyword) ||
        i.cnname.toLowerCase().includes(keyword)
      );
    });
  }
  // 重置页码
  currentPage.value = 1;
}

function resetFilters() {
  filter.value.cnname = '';
  searchText.value = '';
  filtered.value = [...rawData.value];
  currentPage.value = 1;
}

function openDetail(item: RecordItem) {
  detail.value = item;
  showDetail.value = true;
}

// 监听分页边界
watch([currentPage, pageSize], () => {
  if (currentPage.value > pageCount.value) currentPage.value = pageCount.value;
});
</script>

<style scoped>
/* 你可以改成你设计系统的 class */
table th, table td { vertical-align: top; }
</style>
