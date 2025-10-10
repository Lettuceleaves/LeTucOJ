<template>
  <div v-html="mdContent"></div>
</template>

<script setup lang="ts">
import { marked } from 'marked';
import { computed } from 'vue';

const { mdText } = defineProps<{
  mdText?: string
}>();

const mdContent = computed(() => {
  if (!mdText) return;
  return marked(mdText);
});
</script>
