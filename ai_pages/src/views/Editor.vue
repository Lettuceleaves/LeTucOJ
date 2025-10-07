<template>
  <div class="online-editor">
    <!-- 标题栏 -->
    <div class="title-bar">
      <div class="title-item" :class="{active:activeTab==='do'}"     @click="activeTab='do'">做题</div>
      <div class="title-item" :class="{active:activeTab==='solution'}" @click="activeTab='solution'">题解</div>
      <div class="title-item" :class="{active:activeTab==='result'}"   @click="activeTab='result'">结果</div>
      <div class="title-item" :class="{active:activeTab==='ai'}"       @click="activeTab='ai'">AI</div>
      <button class="back-btn" @click="goBack">返回</button>
    </div>
    <!-- 内容区域 -->
    <div class="content">
      <DoPage
        v-show="activeTab==='do'"
        :editorReady="editorReady"
        :problemData="problemData"
        @exit="goToLogin"
        @submit="handleSubmit"
        @check="checkCode"
        ref="doPageRef"
      />
      <SolutionPage v-show="activeTab === 'solution'" :solution="solutionContent" />
      <ResultPage v-show="activeTab === 'result'" :result="resultData" />
      <AiChat v-show="activeTab === 'ai'" ref="aiChatRef" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, nextTick, getCurrentInstance, provide, readonly, onUnmounted } from 'vue' // <-- 1. 导入 onUnmounted
import { useRouter } from 'vue-router'

import DoPage from './EditorPages/DoPage.vue'
import SolutionPage from './EditorPages/SolutionPage.vue'
import ResultPage from './EditorPages/ResultPage.vue'
import AiChat from './EditorPages/AiChat.vue'

const router = useRouter()
const props = defineProps({ name: { type: String, required: true } })
const name = props.name

const activeTab = ref('do')
const editorReady = ref(false)
const result = ref(null)
const solutionContent = ref('')
const problemData = ref({})
const aiChatRef = ref(null)

const resultData = computed(() => result.value || { code: '-1' })
const instance = getCurrentInstance()
const ip = instance.appContext.config.globalProperties.$ip

const doPageRef = ref(null)

const aiContext = computed(() => {
      const context = doPageRef.value?.getAIContext?.()
      return {
            problem: context?.problem || {},
            language: context?.language || selectedLanguage.value || 'unknown',
            code: context?.code || ''
      }
})

// 1. 创建响应式变量
const selectedLanguage = ref('c')

// 2. 提供“读”和“写”两个注入 key
provide('lang', readonly(selectedLanguage))    // 只读，防止孙子直接改
provide('setLang', (val) => { selectedLanguage.value = val })

const goBack = () => {
   router.back()   // 或 router.go(-1)
}


// 统一的提交处理函数
const handleSubmit = () => {
   const userCode = doPageRef.value?.getCode?.()
   if (!userCode) {
      alert('无法获取代码内容')
      return
   }

   console.log('开始处理提交')

   // 1. 执行原来的提交操作
   submitCode(userCode)

   console.log('代码已提交，准备发送给AI')

   // 2. 同时发送给AI分析
   sendToAI()
}

// 发送给AI分析
const sendToAI = () => {

   // 确保AI聊天组件已经渲染
   nextTick(() => {
      if (aiChatRef.value) {
         console.log('AI组件引用存在')

         // 收集完整的 AI 上下文
         const contextData = aiContext.value;
         
         // *** 用户的修改逻辑：序列化 aiContext 并加入提示词 ***
         const serializedContext = JSON.stringify(contextData, null, 2);
         
         // 基础提示词
         const basePrompt = `请分析我刚刚提交的代码，并给出优化建议。`;
         
         // 将序列化后的上下文附加到提示词后面
         const fullPrompt = `${basePrompt}\n\n--- 上下文数据 ---\n\`\`\`json\n${serializedContext}\n\`\`\``;
         // *** 用户的修改逻辑结束 ***

         console.log('发送给AI的完整提示:', fullPrompt)

         try {
            // 调用AI聊天组件的发送消息方法，仅传递包含所有信息的 fullPrompt
            // AiChat.vue 的 sendMessage 只需要接受这个完整的文本即可
            aiChatRef.value.sendMessage(fullPrompt)
            console.log('AI分析请求发送成功，已将上下文附加到提示词中。')
         } catch (e) {
            console.error('调用AI组件方法失败:', e)
         }
      } else {
         console.error('AI组件引用不存在')
      }
   })
}

const sendCode = async (userCode) => {
   console.log('发送代码到后端:', userCode ? '代码存在' : '代码不存在')
   activeTab.value = 'result'

   result.value = {
      code: '-1',
      data: "正在提交，请等待"
   }
   try {
      const token = localStorage.getItem('jwt')
      const params = new URLSearchParams({
         qname: name,
         language: selectedLanguage.value
      });
      const response = await fetch(`http://${ip}/practice/submit?${params}`, {
         method: 'POST',
         headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
         },
         body: userCode
      })
      const data = await response.json()

      result.value = {
         code: data.code,
         data: data.data,
         dataAsString: data.dataAsString,
         error: data.error || null,
      }
   } catch (error) {
      console.error('发送代码出错:', error)
      result.value = {
         code: '-1',
         data: null,
         dataAsString: null,
         error: error.message || '未知错误',
      }
   }
}

// 修改为接收代码参数的函数
const submitCode = (userCode) => sendCode(userCode)

const checkCode = () => {
   const userCode = doPageRef.value?.getCode?.()
   if (userCode) {
      sendCode(userCode)
   } else {
      alert('无法获取代码内容')
   }
}

const goToLogin = () => router.push('/login')

const fetchDataOnRefresh = async () => {
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
      if (!response.ok) throw new Error(`请求失败，状态码：${response.code}`)

      const data = await response.json()
      problemData.value = data.data || {}
      solutionContent.value = problemData.value.solution || '暂无题解'
   } catch (error) {
      alert(`获取失败：${error.message}`)
      solutionContent.value = '加载题解失败'
   }
}

watch(activeTab, async (newVal, oldVal) => {
   // 当离开 'do' 标签页时，将当前编辑器中的代码保存到临时存储。
   if (oldVal === 'do') {
      const code = doPageRef.value?.getCode?.()
      if (code) {
         // 注意：这里保存的是单语言的备份，我们应该使用组件内部的 userCodeStorage
         // 但由于 DoPage/Monaco 内部逻辑已经管理了 userCodeStorage，
         // 这里的逻辑如果只针对单语言备份，可能需要调整。
         // 假设这里的 'userCode' 是一个老旧的/临时的单语言备份键。
         localStorage.setItem('userCode', code) 
      }
   }

   // 当进入 'do' 标签页时，恢复代码。
   if (newVal === 'do') {
      const savedCode = localStorage.getItem('userCode')
      // 仅在 savedCode 存在时设置，否则 Monaco 组件会使用默认代码
      if (savedCode) {
         doPageRef.value?.setCode?.(savedCode)
      }
   }
})


onMounted(() => {
   editorReady.value = true
   fetchDataOnRefresh()
})

// --- 新增逻辑：在组件销毁时清空代码备份 ---
onUnmounted(() => {
    // 移除 Monaco 组件内部的跨语言代码存储
    localStorage.removeItem('userCodeStorage');
    console.log('Online Editor 退出：已清空多语言代码备份 (userCodeStorage)。');

    // 移除旧的/单语言的临时代码备份（如果还存在）
    localStorage.removeItem('userCode');
    console.log('Online Editor 退出：已清空单语言临时代码备份 (userCode)。');
});
// ---------------------------------------------
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
