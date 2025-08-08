<template>
  <main-layout selected-tab="submit">
    <el-splitter v-if="!practiceDetail" class="h-full">
      <el-splitter-panel :size="550" :min="400" v-if="isShowDetail" class="pt-8 pr-6 flex flex-col gap-4">
        <el-skeleton animated />
        <el-card class="flex-1">
          <el-skeleton animated />
        </el-card>
      </el-splitter-panel>
      <el-splitter-panel :min="550" class="pt-2 pl-6 flex flex-col gap-4">
        <el-card>
          <el-skeleton animated :rows="0" />
        </el-card>

        <el-card class="flex-1"></el-card>
      </el-splitter-panel>
    </el-splitter>

    <el-splitter v-if="practiceDetail" class="h-full">
      <el-splitter-panel :size="550" :min="400" v-if="isShowDetail" class="pt-8 pr-6 flex flex-col gap-4">
        <el-page-header @back="router.back()">
          <template #content>
            <div class="flex items-baseline">
              <span class="text-6 font-bold">{{ practiceDetail.cnname }}</span>
              <span class="text-4 ml-2 text-gray-500">#{{ practiceDetail.name }}</span>
            </div>
          </template>

          <el-collapse>
            <el-collapse-item title="练习描述" name="description">
              <practice-description :practice-detail="practiceDetail" />
            </el-collapse-item>
          </el-collapse>
        </el-page-header>

        <el-card class="flex-1 overflow-auto">
          <markdown-section :md-text="practiceDetail.content" />
        </el-card>
      </el-splitter-panel>

      <el-splitter-panel :min="550" class="pt-2 pl-6 flex flex-col gap-4">
        <el-card body-class="flex flex-row gap-2">
          <el-button @click="handleToggleShowDetail">
            <el-icon v-if="isShowDetail">
              <ArrowLeftBold />
            </el-icon>
            <el-icon v-if="!isShowDetail">
              <ArrowRightBold />
            </el-icon>
          </el-button>

          <el-segmented v-model="selectedTab" :options="tabs" />

          <div class="flex-1"></div>

          <el-select class="w-32" v-model="language" placeholder="选择语言">
            <el-option label="C" value="c" />
            <!-- <el-option label="Typescript" value="typescript" /> -->
            <el-option label="C#" value="csharp" disabled />
            <el-option label="C++" value="cpp" disabled />
            <el-option label="Java" value="java" disabled />
            <el-option label="Python" value="python" disabled />
          </el-select>

          <!-- <el-button type="success">测试</el-button> -->
          <el-button type="primary" :disabled="isSubmitting" @click="handleSubmitClick">提交</el-button>
        </el-card>

        <editor-card v-if="selectedTab === 'editor'" v-model:code="code" :language="language" />

        <el-card v-if="selectedTab === 'answer'" class="flex-1">
          <markdown-section :md-text="practiceDetail.solution" />
        </el-card>

        <el-card v-if="selectedTab === 'history'" class="flex-1">

        </el-card>
      </el-splitter-panel>
    </el-splitter>
  </main-layout>
</template>

<script setup lang="ts">
import { GetPracticeDetailRequest, SubmitPracticeRequest } from '@/apis/Practice';
import MainLayout from '@/components/MainLayout.vue';
import PracticeDescription from '@/components/PracticeDescription.vue';
import MarkdownSection from '@/components/MarkdownSection.vue';
import EditorCard from '@/components/EditorCard.vue';
import type { PraciceInfo } from '@/models/Practice';
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

type Tab = 'editor' | 'answer' | 'history';

const route = useRoute();
const router = useRouter();

const practiceName = route.params.name as string;
const practiceDetail = ref<PraciceInfo>();

const isShowDetail = ref(true);
const selectedTab = ref<Tab>('editor');

const code = ref('');
const language = ref('c');
const isSubmitting = ref(false);

const tabs = computed(() => {
  const allTabs = [
    { label: '编辑器', value: 'editor' },
    { label: '题解', value: 'answer' },
    { label: '历史提交', value: 'history' }
  ];

  if (!practiceDetail.value?.showsolution) {
    return allTabs.filter(tab => tab.value !== 'answer');
  }
  return allTabs;
});

const handleToggleShowDetail = () => {
  isShowDetail.value = !isShowDetail.value;
}

const handleSubmitClick = async () => {
  try{
    isSubmitting.value = true;
    const resp = await new SubmitPracticeRequest(practiceName, language.value, code.value).request();

    if (resp.status !== 0) {
      console.error('提交失败:', resp.error);
      return;
    }
    console.log('提交成功:', resp.data);
  }
  finally {
    isSubmitting.value = false;
  }
}

onMounted(async () => {
  let response = await new GetPracticeDetailRequest(practiceName).request();
  if (response.status !== 0) return

  practiceDetail.value = response.data!;
})
</script>
