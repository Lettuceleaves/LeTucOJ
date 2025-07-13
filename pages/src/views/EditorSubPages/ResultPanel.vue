<!-- src/components/ResultPanel.vue -->
<template>
  <div>
    <p v-if="result === null">暂无结果</p>
    <p v-else>{{ result }}</p>
  </div>
</template>

<script setup>
defineProps({
  result: {
    type: String,
    default: null
  }
});
</script>
