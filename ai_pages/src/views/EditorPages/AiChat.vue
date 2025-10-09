<template>
  <div class="card ai-card">
    <h3 class="title">AI 聊天</h3>
    <div class="ai-window" ref="aiWindow">
      <div
        v-for="(msg, index) in messages"
        :key="index"
        :class="['ai-message', msg.role]"
      >
        <strong class="role-label">
          {{ msg.role === 'user' ? '我' : 'AI' }}：
        </strong>
        <div v-if="msg.role === 'user'" class="msg-content">
          {{ msg.content }}
        </div>
        <div v-else class="msg-content">
          <MarkdownRenderer :raw-content="msg.raw" />
        </div>
      </div>
    </div>
    <div class="ai-input">
      <input
        v-model="inputText"
        @keyup.enter="sendMessage(inputText)"
        placeholder="请输入你的问题..."
      />
      <button @click="sendMessage(inputText)">发送</button>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, onUnmounted, getCurrentInstance } from 'vue'
import MarkdownRenderer from '../../components/MarkdownRenderer.vue'; // 确保路径正确


defineExpose({
  sendMessage
})

const instance = getCurrentInstance()
// 使用 ?. 避免在没有全局属性时出错
const ip = instance?.appContext.config.globalProperties.$ip || 'localhost'

const messages = ref([])
const inputText = ref('')
const aiWindow = ref(null)
let eventSource = null


async function sendMessage(text, context = '') {
  const trimmed = text.trim()
  const token = localStorage.getItem('jwt') || ''
  if (!trimmed) return

  messages.value.push({ role: 'user', content: trimmed })
  // 仅初始化 raw 字段
  messages.value.push({ role: 'assistant', raw: '' }) 
  const idx = messages.value.length - 1
  inputText.value = ''
  scrollToBottom()

  const res = await fetch(`http://${ip}/advice`, {
    method: 'POST',
    headers: { 
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token}` 
    },
    body: JSON.stringify({ userFile: trimmed + context })
  })

  if (!res.body) {
    // 简化错误处理，只设置 raw
    messages.value[idx].raw = '错误：无法获取流响应体。'
    return
  }

  const reader = res.body
    .pipeThrough(new TextDecoderStream())
    .getReader()

  
  let currentRaw = ''; // 💡 修复 1: 在循环外初始化，用于累积所有数据块
  let hasNewChunk = false; // 标记是否有新数据进来

  while (true) {
    const { done, value } = await reader.read()
    
    if (done) {
        // 最终清理
        messages.value[idx].raw = currentRaw.trimEnd();
        scrollToBottom()
        break
    }
    
    const lines = value ? value.split(/\r?\n/) : []
    
    for (const line of lines) {
      if (line.startsWith('data:')) {
        let chunk = line.slice(5).trimStart()

        if (chunk.includes('\\x0A') || chunk.includes('\\x0B')) {
          chunk = chunk.replace(/\\x0A/g, '\n'); 
          chunk = chunk.replace(/\\x0B/g, ' '); 
        }
        
        if (chunk !== '') {
          // 💡 修复 2: 将 chunk 累加到 currentRaw
          currentRaw += chunk; 
          hasNewChunk = true; // 💡 修复 3: 标记有新内容
        }
      }
    }
    
    // 检查是否有内容更新，并通知 Vue 更新 raw
    if (hasNewChunk) {
        messages.value[idx].raw = currentRaw; // 💡 修复 4: 无条件更新 raw，保持流式输出
        hasNewChunk = false;
        // 滚动到底部，以便用户能实时看到输出
        scrollToBottom() 
    }
  }
}

function scrollToBottom() {
  nextTick(() => {
    if (aiWindow.value) {
      aiWindow.value.scrollTop = aiWindow.value.scrollHeight
    }
  })
}

onUnmounted(() => {
  if (eventSource) {
    eventSource.close()
    eventSource = null
  }
})
</script>
<style scoped>
/* (你的样式代码不变) */
.card {
  background: #fff;
  border: 1px solid #ddd;
  border-radius: 4px;
  padding: 16px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  margin: 16px;
}
.title {
  margin: 0 0 12px;
  font-size: 1.25rem;
}
.ai-window {
  height: 65vh;
  overflow-y: auto;
  border: 1px solid #eee;
  padding: 8px;
  background: #f9f9f9;
}
.ai-message {
  margin-bottom: 12px;
  line-height: 1.6;
}
.ai-message.user .role-label {
  color: #2563eb;
}
.ai-message.assistant .role-label {
  color: #059669;
}
.msg-content {
  margin-left: 4px;
  word-wrap: break-word;
}
.ai-input {
  display: flex;
  margin-top: 12px;
}
.ai-input input {
  flex: 1;
  padding: 8px;
  border: 1px solid #ccc;
  border-radius: 4px;
}
.ai-input button {
  margin-left: 8px;
  padding: 8px 16px;
  background: #3b82f6;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}
.ai-input button:hover {
  background: #2563eb;
}

.loading {
  color: #666;
  font-style: italic;
}

.error {
  color: #dc3545;
}
</style>