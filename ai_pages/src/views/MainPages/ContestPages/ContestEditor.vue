<template>
  <div class="online-editor">
    <!-- 标题栏 -->
    <div class="title-bar">
      <div class="title-item" :class="{ active: activeTab === 'back' }" @click="goBack">返回</div>

      <div
        class="title-item"
        :class="{ active: activeTab === 'description' }"
        @click="activeTab = 'description'"
      >
        描述
      </div>

      <div
        class="title-item"
        :class="{ active: activeTab === 'question' }"
        @click="activeTab = 'question'"
      >
        答题
      </div>

      <div
        class="title-item"
        :class="{ active: activeTab === 'result' }"
        @click="activeTab = 'result'"
      >
        结果
      </div>
    </div>

    <!-- 内容区域 -->
    <div class="content">
      <DescriptionPage v-show="activeTab === 'description'" :data="problemData" />
      <QuestionPage
        v-show="activeTab === 'question'"
        :editorReady="editorReady"
        @exit="goToLogin"
        @submit="handleSubmit"
        @check="checkCode"
        ref="questionPageRef"
      />
      <ResultPage v-show="activeTab === 'result'" :result="resultData" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, nextTick, getCurrentInstance } from 'vue'
import { useRouter, useRoute } from 'vue-router'

import DescriptionPage from './EditorPages/DescriptionPage.vue'
import QuestionPage from './EditorPages/QuestionPage.vue'
import ResultPage from './EditorPages/ResultPage.vue'

const router = useRouter()
const route = useRoute()
const contest = computed(() => route.query.contest || '')
const problem = computed(() => route.query.problem || '')

const activeTab = ref('description')
const editorReady = ref(true)
const result = ref(null)
const problemData = ref({})
const instance = getCurrentInstance()

const REMOTE_SERVER_URL = import.meta.env.VITE_REMOTE_SERVER_URL

const questionPageRef = ref(null)
const resultData = computed(() => result.value || { status: -1 })

const sendCode = async (code) => {
  activeTab.value = 'result'
  try {
    const token = localStorage.getItem('jwt')
    const params = new URLSearchParams({
      qname: problem.value, // ✅ 使用 route.query
      ctname: contest.value, // ✅ 使用 route.query
      lang: 'C',
    })
    console.log('提交参数', params.toString())

    const response = await fetch(`${REMOTE_SERVER_URL}/contest/submit?${params}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${token}`,
      },
      body: code,
    })
    const data = await response.json()
    result.value = {
      status: data.status,
      data: data.data,
      dataAsString: data.dataAsString,
      error: data.error || null,
    }
  } catch (error) {
    result.value = { status: -1, error: error.message || '未知错误' }
  }
}

const handleSubmit = () => {
  const code = questionPageRef.value?.getCode()
  if (!code) {
    alert('代码为空')
    return
  }
  sendCode(code)
}

const goBack = () => {
  router.back() // 返回上一页，也可用 router.push('/指定路径')
}

const checkCode = () => {
  const code = questionPageRef.value?.getCode()
  code ? sendCode(code) : alert('无法获取代码内容')
}

const goToLogin = () => router.push('/login')

const fetchDataOnRefresh = async () => {
  try {
    const token = localStorage.getItem('jwt')
    const params = new URLSearchParams({
      qname: problem.value,
      ctname: contest.value,
    })

    const response = await fetch(`${REMOTE_SERVER_URL}/practice/full/get?${params}`, {
      method: 'GET',
      headers: { Authorization: `Bearer ${token}` },
    })
    if (!response.ok) throw new Error(`请求失败，状态码：${response.status}`)
    const data = await response.json()
    problemData.value = data.data || {}
  } catch (error) {
    alert(`获取失败：${error.message}`)
  }
}

watch(activeTab, async (newVal, oldVal) => {
  await nextTick()
  if (oldVal === 'question') {
    const code = questionPageRef.value?.getCode?.()
    if (code) localStorage.setItem('userCode', code)
  }
  if (newVal === 'question') {
    const savedCode = localStorage.getItem('userCode')
    questionPageRef.value?.setCode?.(savedCode)
  }
})

onMounted(() => {
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
