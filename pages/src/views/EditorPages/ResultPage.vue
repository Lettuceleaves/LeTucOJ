<template>
  <div class="card">
    <h3 class="title">运行结果</h3>
    <div :class="['result-box', resultStatusClass]">
      <p><strong>状态码：</strong>{{ result.status }}</p>
      <p><strong>输出信息：</strong>{{ result.dataAsString || result.data || '无输出' }}</p>
      <p v-if="result.error"><strong>错误信息：</strong>{{ result.error }}</p>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  result: Object,
})

const resultStatusClass = computed(() => {
  switch (props.result.status) {
    case 0:
      return 'result-success' // accept
    case 1:
      return 'result-fail' // wrong
    case 2:
      return 'result-compile-error'
    case 3:
      return 'result-runtime-error'
    case 4:
      return 'result-timeout'
    case 5:
      return 'result-server-error'
    default:
      return 'result-unknown'
  }
})
</script>

<style scoped>
.result-success {
  background-color: #e6ffed;
  border: 1px solid #34d399;
}

.result-fail {
  background-color: #ffe4e6;
  border: 1px solid #f87171;
}

.result-compile-error {
  background-color: #fff3cd;
  border: 1px solid #facc15;
}

.result-runtime-error {
  background-color: #fde2e1;
  border: 1px solid #f97316;
}

.result-timeout {
  background-color: #fef9c3;
  border: 1px solid #eab308;
}

.result-server-error {
  background-color: #f3e8ff;
  border: 1px solid #8b5cf6;
}

.result-unknown {
  background-color: #f3f4f6;
  border: 1px solid #d1d5db;
}

</style>
