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
        :class="{ active: activeTab === 'solution' }"
        @click="activeTab = 'solution'"
      >
        题解
      </div>
      <div
        class="title-item"
        :class="{ active: activeTab === 'result' }"
        @click="activeTab = 'result'"
      >
        结果
      </div>
      <div class="title-item" :class="{ active: activeTab === 'ai' }" @click="activeTab = 'ai'">
        AI
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
      <SolutionPage v-show="activeTab === 'solution'" :solution="solutionContent" />
      <ResultPage v-show="activeTab === 'result'" :result="resultData" />
      <AiChat v-show="activeTab === 'ai'" ref="aiChatRef" />
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
import AiChat from './EditorPages/AiChat.vue'

const router = useRouter()
const props = defineProps({ name: { type: String, required: true } })
const name = props.name

const activeTab = ref('description')
const editorReady = ref(false)
const result = ref(null)
const solutionContent = ref('')
const problemData = ref({})
const aiChatRef = ref(null)

const resultData = computed(() => result.value || { status: -1 })
const instance = getCurrentInstance()
const REMOTE_SERVER_URL = import.meta.env.VITE_REMOTE_SERVER_URL

const questionPageRef = ref(null)

// 统一的提交处理函数
const handleSubmit = () => {
  const code = questionPageRef.value?.getCode()
  if (!code) {
    alert('无法获取代码内容')
    return
  }

  console.log('开始处理提交')

  // 1. 执行原来的提交操作
  submitCode(code)

  // 2. 同时发送给AI分析
  sendToAI(code)
}

// 发送给AI分析
const sendToAI = (code) => {
  console.log('准备发送给AI:', code ? '代码存在' : '代码不存在')

  // 确保AI聊天组件已经渲染
  nextTick(() => {
    if (aiChatRef.value) {
      console.log('AI组件引用存在')

      // 添加提示词让AI分析代码
      const prompt = `请分析以下JavaScript代码：\n\`\`\`javascript\n${code}\n\`\`\`\n`
      console.log('发送给AI的提示:', prompt)

      try {
        // 调用AI聊天组件的发送消息方法
        aiChatRef.value.sendMessage(prompt)
        console.log('AI消息发送成功')

        // 切换到AI标签页
        activeTab.value = 'ai'
      } catch (e) {
        console.error('调用AI组件方法失败:', e)
      }
    } else {
      console.error('AI组件引用不存在')
    }
  })
}

const sendCode = async (code) => {
  console.log('发送代码到后端:', code ? '代码存在' : '代码不存在')
  activeTab.value = 'result'

  try {
    const token = localStorage.getItem('jwt')
    const params = new URLSearchParams({
      qname: name,
      lang: 'C',
    })
    const response = await fetch(`${REMOTE_SERVER_URL}/practice/submit?${params}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${token}`,
      },
      body: code,
    })
    const data = await response.json()
    console.log('后端响应数据:', data)

    result.value = {
      status: data.status,
      data: data.data,
      dataAsString: data.dataAsString,
      error: data.error || null,
    }
  } catch (error) {
    console.error('发送代码出错:', error)
    result.value = {
      status: -1,
      data: null,
      dataAsString: null,
      error: error.message || '未知错误',
    }
  }

  /*
  
    try {
    const token = localStorage.getItem('jwt')
    const params = new URLSearchParams({
      qname: name
    });
    const response = await fetch(`http://${ip}/practice/full/get?${params}`, {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${token}`
      }
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
  
  */
}

// 修改为接收代码参数的函数
const submitCode = (code) => sendCode(code)

const checkCode = () => {
  const code = questionPageRef.value?.getCode()
  if (code) {
    sendCode(code)
  } else {
    alert('无法获取代码内容')
  }
}

const goToLogin = () => router.push('/login')

const fetchDataOnRefresh = async () => {
  try {
    const token = localStorage.getItem('jwt')
    const params = new URLSearchParams({
      qname: name,
    })
    const response = await fetch(`http://${ip}/practice/full/get?${params}`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
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
  await nextTick()

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
