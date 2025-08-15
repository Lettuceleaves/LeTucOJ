<template>
  <div class="card ai-card">
    <h3 class="title">AI 聊天</h3>
    <div class="ai-window" ref="aiWindow">
      <div v-for="(msg, index) in messages" :key="index" :class="['ai-message', msg.role]">
        <strong class="role-label"> {{ msg.role === 'user' ? '我' : 'AI' }}： </strong>
        <div v-if="msg.role === 'user'" class="msg-content">
          {{ msg.content }}
        </div>
        <div v-else class="msg-content markdown-body" v-html="msg.html"></div>
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
import { marked } from 'marked'
import DOMPurify from 'dompurify'

defineExpose({
  sendMessage,
})

// 配置marked - 修复标题渲染问题
marked.setOptions({
  breaks: true,
  gfm: true,
  headerIds: false,
  mangle: false,
  // 确保标题正确解析
  headerPrefix: '',
  // 修复中文标题问题
  langPrefix: 'language-',
  // 自定义标题渲染
  renderer: new marked.Renderer(),
})

// 全局IP配置
const instance = getCurrentInstance()
const REMOTE_SERVER_URL = import.meta.env.VITE_REMOTE_SERVER_URL || 'localhost'

const messages = ref([])
const inputText = ref('')
const aiWindow = ref(null)
let eventSource = null

// 安全渲染Markdown - 修复标题问题
const renderMarkdown = (content) => {
  if (!content) return ''

  // 修复标题格式：确保 # 后有空格
  const fixedContent = content.replace(/(^|\n)(#+)([^#\s])/g, '$1$2 $3')

  try {
    const rawHtml = marked(fixedContent)
    return DOMPurify.sanitize(rawHtml)
  } catch (error) {
    console.error('Markdown渲染错误:', error)
    return `<div class="error">渲染错误: ${error.message}</div>`
  }
}

// 滚动到底部
function scrollToBottom() {
  nextTick(() => {
    if (aiWindow.value) {
      aiWindow.value.scrollTop = aiWindow.value.scrollHeight
    }
  })
}

// 发送消息并流式接收AI回复
function sendMessage(text) {
  const trimmed = text.trim()
  if (!trimmed) return

  // 显示用户消息
  messages.value.push({ role: 'user', content: trimmed })

  // 添加AI占位消息
  messages.value.push({
    role: 'assistant',
    raw: '',
    html: '<div class="loading">AI正在思考...</div>',
  })
  const idx = messages.value.length - 1

  inputText.value = ''
  scrollToBottom()

  // 关闭已有SSE
  if (eventSource) {
    eventSource.close()
    eventSource = null
  }

  // 建立新的SSE连接
  eventSource = new EventSource(
    `${REMOTE_SERVER_URL}:9999/advice?userFile=${encodeURIComponent(trimmed)}`,
  )

  // 接收流式数据
  eventSource.onmessage = (e) => {
    const msg = messages.value[idx]
    msg.raw += e.data
    msg.html = renderMarkdown(msg.raw)
    scrollToBottom()
  }

  eventSource.onerror = () => {
    if (eventSource) {
      eventSource.close()
      eventSource = null
    }
  }
}

onUnmounted(() => {
  if (eventSource) {
    eventSource.close()
    eventSource = null
  }
})
</script>

<style scoped>
/* 原有样式保持不变 */
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
</style>

<style>
/* 增强标题样式 - 确保标题明显可见 */
.markdown-body h1 {
  font-size: 2em !important;
  font-weight: bold !important;
  margin: 0.67em 0 !important;
  border-bottom: 1px solid #eaecef !important;
  padding-bottom: 0.3em !important;
  color: #24292e !important;
}

.markdown-body h2 {
  font-size: 1.5em !important;
  font-weight: bold !important;
  margin: 0.83em 0 !important;
  border-bottom: 1px solid #eaecef !important;
  padding-bottom: 0.3em !important;
  color: #24292e !important;
}

.markdown-body h3 {
  font-size: 1.25em !important;
  font-weight: bold !important;
  margin: 1em 0 !important;
  color: #24292e !important;
}

.markdown-body h4 {
  font-size: 1.1em !important;
  font-weight: bold !important;
  margin: 1.33em 0 !important;
  color: #24292e !important;
}

/* 其他样式保持不变 */
.markdown-body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Helvetica, Arial, sans-serif;
  font-size: 16px;
  line-height: 1.6;
  word-wrap: break-word;
}

.markdown-body p {
  margin-top: 0;
  margin-bottom: 1em;
}

.markdown-body pre {
  background-color: #f6f8fa;
  border-radius: 6px;
  padding: 16px;
  overflow: auto;
  margin-bottom: 1em;
}

.markdown-body code {
  background-color: rgba(175, 184, 193, 0.2);
  border-radius: 3px;
  padding: 0.2em 0.4em;
  font-family:
    ui-monospace,
    SFMono-Regular,
    SF Mono,
    Menlo,
    Consolas,
    Liberation Mono,
    monospace;
  font-size: 85%;
}

.markdown-body pre code {
  background-color: transparent;
  padding: 0;
  font-size: 100%;
}

.markdown-body blockquote {
  border-left: 4px solid #dfe2e5;
  color: #6a737d;
  padding: 0 1em;
  margin: 0 0 1em 0;
}

.markdown-body ul,
.markdown-body ol {
  padding-left: 2em;
  margin-bottom: 1em;
}

.markdown-body li {
  margin-bottom: 0.5em;
}

.loading {
  color: #666;
  font-style: italic;
}

.error {
  color: #dc3545;
}
</style>
