<template>
  <div class="desc-panel card">
    <h3 class="title">{{ data.cnname }}（{{ data.name }}）</h3>
    <p><strong>测试点数量：</strong>{{ data.caseAmount }}</p>
    <p><strong>题目描述：</strong></p>
    
    <MarkdownRenderer 
      :raw-content="data.content || '暂无描述'" 
      container-class="paragraph" 
    />
  </div>
</template>

<script setup>
import MarkdownRenderer from '../../components/MarkdownRenderer.vue'; //

const props = defineProps({
  data: Object,
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
  /* 确保这个容器允许正常的文本折行 */
  white-space: normal; 
}

</style>