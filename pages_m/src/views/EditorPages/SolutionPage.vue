<template>
  <div class="card">
    <h3 class="title">题解</h3>
    <p class="paragraph">{{ solution }}</p>
  </div>
</template>

<script setup>
const props = defineProps({
  solution: String,
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
  white-space: pre-wrap;
}
</style>
