<template>
  <main-layout selected-tab="submit">
    <el-splitter v-if="practiceDetail" class="h-full">
      <el-splitter-panel size="550px" class="pt-8 pr-6 flex flex-col gap-4">
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

      <el-splitter-panel class="pt-2 pl-6 flex flex-col gap-4">
        <el-card body-class="flex flex-row gap-2">
          <el-button-group>
            <el-button @click="handleTabClick('editor')" :type="selectedTab === 'editor' ? 'primary' : ''">编辑器</el-button>
            <el-button @click="handleTabClick('answer')" :type="selectedTab === 'answer' ? 'primary' : ''">题解</el-button>
          </el-button-group>

          <div class="flex-1"></div>

          <el-select class="w-32 mx-4" v-model="language" placeholder="选择语言">
            <el-option label="C" value="c"  />
            <el-option label="C++" value="cpp" disabled />
            <el-option label="Java" value="java" disabled />
            <el-option label="Python" value="python" disabled />
          </el-select>

          <el-button type="success">测试</el-button>
          <el-button type="primary">提交</el-button>
        </el-card>

        <el-card class="editor-card flex-1" body-class="h-full p-0">
          <monaco-editor class="h-full" theme="vs-dark" v-model:value="code" :language="language" :options="editorOptions" />
        </el-card>
      </el-splitter-panel>
    </el-splitter>
  </main-layout>
</template>

<script setup lang="ts">
import { GetPracticeDetailRequest } from '@/apis/Practice';
import MainLayout from '@/components/MainLayout.vue';
import PracticeDescription from '@/components/PracticeDescription.vue';
import MarkdownSection from '@/components/MarkdownSection.vue';
import type { PraciceInfo } from '@/models/Practice';
import { onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import MonacoEditor from 'monaco-editor-vue3';

const route = useRoute();
const router = useRouter();

const practiceName = route.params.name as string;
const practiceDetail = ref<PraciceInfo>();

const selectedTab = ref<'editor' | 'answer'>('editor');

const code = ref('');
const language = ref('c');
const editorOptions = {
  automaticLayout: true,
  fontSize: 18,
  fontFamily: 'var(--el-font-family-monospace)',
  fontLigatures: true,
};

const handleTabClick = (tab: 'editor' | 'answer') => {
  selectedTab.value = tab;
}

onMounted(async () => {
  let response = await new GetPracticeDetailRequest(practiceName).request();
  if (response.status !== 0) return

  practiceDetail.value = response.data!;
})
</script>
