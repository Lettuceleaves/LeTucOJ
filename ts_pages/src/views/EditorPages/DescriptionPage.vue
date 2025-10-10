<template>
  <div class="desc-panel card">
    <h3 class="title">{{ data.cnname }}（{{ data.name }}）</h3>
    <p><strong>测试点数量：</strong>{{ data.caseAmount }}</p>
    <p><strong>题目描述：</strong></p>
    <div class="paragraph" v-html="parsedMarkdown" />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { marked } from 'marked'

const props = defineProps({
  data: Object,
})

const parsedMarkdown = computed(() => {
  // 获取内容，如果没有内容，使用默认内容
  const content = props.data?.content || '暂无描述'
  
  // 使用 marked 库解析 Markdown
  const htmlContent = marked(content)
  
  return htmlContent
})
</script>

<style scoped>
.card {
  background: #ffffff;
  margin: 20px auto;
  padding: 24px;
  max-width: 900px;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  transition: box-shadow 0.3s ease;
}

.card:hover {
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.12);
}

.title {
  font-size: 20px;
  font-weight: bold;
  margin-bottom: 12px;
  color: #1f2937;
}

.paragraph {
  font-size: 16px;
  line-height: 1.6;
  color: #374151;
  white-space: normal;
}

/* 可选：Markdown 样式优化 */
.paragraph h1,
.paragraph h2,
.paragraph h3 {
  margin-top: 16px;
  font-weight: bold;
}

.paragraph code {
  background-color: #f3f4f6;
  padding: 2px 4px;
  border-radius: 4px;
  font-family: monospace;
}
</style>
