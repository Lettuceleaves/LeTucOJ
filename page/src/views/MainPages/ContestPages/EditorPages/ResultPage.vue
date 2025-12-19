<template>
  <div class="card">
    <h3 class="title">运行结果</h3>
    <div :class="['result-box', resultStatusClass]">
      <p><strong>状态：</strong>{{ resultStatusText }}</p>
      <p><strong>输出信息：</strong>{{result.data || '无输出' }}</p>
      <p v-if="result.error"><strong>错误信息：</strong>{{ result.message }}</p>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  result: Object,
})

const statusTextMap = {
  "0": '通过',
  'B010005': '答案错误',
  'B010006': '编译错误',
  'B010007': '运行时错误',
  'B010008': '超时',
  '5': '服务器错误',
  '-1': '未知状态',
}

const resultStatusText = computed(() => {
  return statusTextMap[props.result.code] ?? '未知状态'
})

const resultStatusClass = computed(() => {
  switch (props.result.code) {
    case "0":
      return 'result-success'
    case 'B010005':
      return 'result-fail'
    case 'B010006':
      return 'result-compile-error'
    case 'B010007':
      return 'result-runtime-message'
    case 'B010008':
      return 'result-timeout'
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

.result-runtime-message {
  background-color: #fde2e1;
  border: 1px solid #f97316;
}

.result-timeout {
  background-color: #d8f2d8;
  border: 1px solid #6f9877;
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
