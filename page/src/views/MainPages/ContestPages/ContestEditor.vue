<template>
  <div class="online-editor">
    <!-- 标题栏 -->
    <div class="title-bar">

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
      <button class="back-btn" @click="goBack">返回</button>
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
import { ref, computed, onMounted, watch, nextTick, getCurrentInstance, provide, readonly } from 'vue'
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
const ip = instance.appContext.config.globalProperties.$ip

const questionPageRef = ref(null)
const resultData = computed(() => result.value || { code: '-1' })

const selectedLanguage = ref('c')

provide('lang', readonly(selectedLanguage))
provide('setLang', (val) => { selectedLanguage.value = val })

const sendCode = async (code) => {
  activeTab.value = 'result'
  result.value = {
    code: '-1',
    data: "正在提交，请等待"
  }
  try {
    const token = localStorage.getItem('jwt')
    const params = new URLSearchParams({
      qname: problem.value,
      ctname: contest.value,
      lang: selectedLanguage.value
    })

    const response = await fetch(`http://${ip}/contest/submit?${params}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${token}`
      },
      body: code
    })
    const data = await response.json()
    result.value = {
      code: data.code,
      data: data.data,
      dataAsString: data.dataAsString,
      error: data.error || null
    }
  } catch (error) {
    result.value = { code: '-1', error: error.message || '未知错误' }
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
  router.back()
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
      ctname: contest.value
    })

    const response = await fetch(`http://${ip}/contest/full/getProblem?${params}`, {
      method: 'GET',
      headers: { Authorization: `Bearer ${token}` }
    })
    if (!response.ok) throw new Error(`请求失败，状态码：${response.code}`)
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
.back-btn {
  background: #ef4444; /* 红色按钮，易识别 */
  color: #fff;
  padding: 8px 14px;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.3s ease;
}
.back-btn:hover {
  background: #dc2626; /* hover 深红色 */
}
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
