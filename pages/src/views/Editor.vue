<template>
  <div class="online-editor">
    <!-- 标题栏 -->
    <div class="title-bar">
      <div class="title-item" :class="{ active: activeTab === 'description' }" @click="activeTab = 'description'">描述</div>
      <div class="title-item" :class="{ active: activeTab === 'question' }" @click="activeTab = 'question'">答题</div>
      <div class="title-item" :class="{ active: activeTab === 'solution' }" @click="activeTab = 'solution'">题解</div>
      <div class="title-item" :class="{ active: activeTab === 'result' }" @click="activeTab = 'result'">结果</div>
    </div>

    <!-- 内容区域 -->
    <div class="content">
      <DescriptionPage v-show="activeTab === 'description'" :data="problemData" />
      <QuestionPage
        v-show="activeTab === 'question'"
        :editorReady="editorReady"
        @exit="goToLogin"
        @submit="submitCode"
        @check="checkCode"
        ref="questionPageRef"
      />
      <SolutionPage v-show="activeTab === 'solution'" :solution="solutionContent" />
      <ResultPage v-show="activeTab === 'result'" :result="resultData" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, nextTick, getCurrentInstance } from 'vue'
import { useRouter } from 'vue-router'

import DescriptionPage from './EditorPages/DescriptionPage.vue'
import QuestionPage from './EditorPages/QuestionPage.vue'
import SolutionPage from './EditorPages/SolutionPage.vue'
import ResultPage from './EditorPages/ResultPage.vue'

const router = useRouter()
const props = defineProps({ name: { type: String, required: true } })
const name = props.name

const instance = getCurrentInstance()
const ip = instance.appContext.config.globalProperties.$ip

const activeTab = ref('description')
const editorReady = ref(false)
const result = ref(null)
const solutionContent = ref('')
const problemData = ref({})

const resultData = computed(() => result.value || { status: -1 })

const questionPageRef = ref(null)

const sendCode = async (action) => {
  const code = questionPageRef.value?.getCode()
  if (!code) {
    alert('无法获取代码内容')
    return
  }
  activeTab.value = 'result'

  try {
    const token = localStorage.getItem('jwt')
    const response = await fetch(`http://${ip}:7777/practice/submitTest`, {
    method: 'POST',
    headers: { 
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${token}`  // 加上这行
  },
  body: JSON.stringify({ service: 0, code, name }),
    })
    const data = await response.json()
    result.value = {
      status: data.status,
      data: data.data,
      dataAsString: data.dataAsString,
      error: data.error || null,
    } 
  } catch (error) {
    result.value = {
      status: -1,
      data: null,
      dataAsString: null,
      error: error.message || '未知错误',
    }
  }
}

const submitCode = () => sendCode('提交')
const checkCode = () => sendCode('检测')
const goToLogin = () => router.push('/login')

const fetchDataOnRefresh = async () => {
  try {
    const token = localStorage.getItem('jwt')
    const response = await fetch(`http://${ip}:7777/practice/fullinfo`, {
      method: 'POST',
      headers: { 
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`  // 加上这行
      },
      body: JSON.stringify({ type: 'SELECT', subType: 'SINGLE_LINE', name, limit: 1, data: {} }),
    })
    if (!response.ok) throw new Error(`请求失败，状态码：${response.status}`)

    const data = await response.json()
    problemData.value = data.data || {}
    solutionContent.value = problemData.value.solution || '暂无题解'
  } catch (error) {
    alert(`获取失败：${error.message}`)
    solutionContent.value = '加载题解失败'
  }
}

watch(activeTab, async (newVal, oldVal) => {
  await nextTick();

  if (oldVal === 'question') {
    try {
      const code = questionPageRef.value?.getCode?.()
      if (code) {
        localStorage.setItem('userCode', code)
      }
    } catch (e) {
      console.warn('保存代码失败：', e)
    }
  }

  if (newVal === 'question') {
    const savedCode = localStorage.getItem('userCode')
    try {
      questionPageRef.value?.setCode?.(savedCode)
    } catch (e) {
      console.warn('加载代码失败：', e)
    }
  }
})

onMounted(() => {
  editorReady.value = true
  fetchDataOnRefresh()
})
</script>

<style>
.online-editor {
  height: 100vh;
  width: 100vw;
  box-sizing: border-box;
}

.title-bar {
  display: flex;
  justify-content: space-around;
  background-color: #f5f5f5;
  padding: 10px 0;
  border-bottom: 1px solid #ccc;
}

.title-item {
  padding: 10px 20px;
  cursor: pointer;
  font-weight: bold;
  transition: background-color 0.2s;
}

.title-item.active {
  background-color: #3b82f6;
  color: white;
}

.title-item:hover {
  background-color: #e0e0e0;
}

.content {
  position: relative;
  padding: 0;
  height: calc(100vh - 50px);
  overflow-y: auto;
}
</style>
