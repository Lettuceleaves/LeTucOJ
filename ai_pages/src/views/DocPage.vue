<template>
  <div class="doc-container">
    <aside class="sidebar">
      <div
        v-for="(heading, index) in headings"
        :key="index"
        class="nav-item"
        :class="{ 'nav-level-1': heading.level === 1 }"
        :style="{ paddingLeft: (heading.level - 1) * 16 + 'px' }"
        @click="scrollToHeading(index)"
      >
        {{ heading.text }}
      </div>
    </aside>

    <main class="content" ref="contentRef">
      <div class="toolbar">
        <button @click="toggleEdit" class="toggle-btn">
          {{ isEdit ? '预览模式' : '编辑模式' }}
        </button>
        <button v-if="isEdit" @click="saveToServer" class="save-btn">保存</button>
      </div>

      <textarea
        v-if="isEdit"
        v-model="rawContent"
        class="editor"
        spellcheck="false"
      ></textarea>

      <div
        v-else
        class="preview markdown-body"
        ref="previewRef"
      >
        <div v-if="renderError" class="error">
          渲染错误: {{ renderError }}
        </div>
        <div v-html="renderedHtml"></div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, watch, getCurrentInstance } from 'vue'
import { marked } from 'marked'
import DOMPurify from 'dompurify'
import axios from 'axios';

const instance = getCurrentInstance()
const ip = instance.appContext.config.globalProperties.$ip

// 配置Marked解析器
marked.setOptions({
  gfm: true,           // 使用GitHub风格的Markdown
  breaks: true,        // 单个换行转换为<br>
  smartLists: true,    // 智能列表
  smartypants: false,  // 禁用智能引号转换
  mangle: false,       // 禁用特殊字符转义
  headerIds: false,    // 禁用自动生成标题ID（避免DOM冲突）
  xhtml: true          // 使用XHTML兼容标签
})

const rawContent = ref('')
const renderedHtml = ref('')
const isEdit = ref(false)
const headings = ref([])
const previewRef = ref(null)
const renderError = ref(null)

async function parseMarkdown(markdown) {
  renderError.value = null
  try {
    // 1. 渲染Markdown为HTML
    const html = marked(markdown)
    
    // 2. 使用DOMPurify消毒HTML内容
    const cleanHtml = DOMPurify.sanitize(html, {
      ADD_ATTR: ['target']  // 允许target属性（用于链接）
    })
    renderedHtml.value = cleanHtml
    
    // 3. 等待DOM更新完成后提取标题
    await nextTick()
    extractHeadingsFromDOM()
    
  } catch (err) {
    console.error('❌ Markdown解析错误:', err)
    renderError.value = err.message
    renderedHtml.value = '<div class="error">渲染出错: ' + err.message + '</div>'
  }
}

function extractHeadingsFromDOM() {
  const newHeadings = []
  if (!previewRef.value) return
  
  // 从实际渲染的DOM中提取标题
  const headingElements = previewRef.value.querySelectorAll('h1, h2, h3, h4, h5, h6')
  
  headingElements.forEach(el => {
    newHeadings.push({
      text: el.textContent || '',
      level: parseInt(el.tagName.substring(1))
    })
  })
  
  headings.value = newHeadings
}

onMounted(async () => {
  try {
    
    const token = localStorage.getItem('jwt')
    const response = await axios.get(`http://${ip}/sys/doc/get`, {
      headers: {
        "Content-Type": "application/json",
        'Authorization': `Bearer ${token}`
      }
    });

    if (response.data.status !== 0) {
      throw new Error('获取文档失败: ' + (response.data.error || '未知错误'));
    }
    
    // 模拟从服务器获取文档
    const mockContent = response.data.data;
    
    rawContent.value = mockContent
    await parseMarkdown(mockContent)
    
  } catch (err) {
    console.error('❌ 加载出错:', err)
    rawContent.value = '# 加载失败\n\n' + err.message
  }
})

function toggleEdit() {
  isEdit.value = !isEdit.value
  if (!isEdit.value) parseMarkdown(rawContent.value)
}

watch(rawContent, (val) => {
  if (!isEdit.value) parseMarkdown(val)
})

async function saveToServer() {
  try {
    
    const token = localStorage.getItem('jwt')
    
    const response = await axios.put(
      `http://${ip}/sys/doc/update`,
      rawContent.value,
      {
        headers: {
          "Content-Type": "text/plain",
          "Authorization": `Bearer ${token}`
        }
      }
    );

    if (response.data.status !== 0) {
      throw new Error('获取文档失败: ' + (response.data.error || '未知错误'));
    }
    alert('保存成功!')
  } catch (err) {
    console.error('保存失败:', err)
    alert('保存失败: ' + err.message)
  }
}

function scrollToHeading(index) {
  const previewEl = previewRef.value
  if (!previewEl) return
  const headingEls = previewEl.querySelectorAll('h1, h2, h3, h4, h5, h6')
  const el = headingEls[index]
  if (el) {
    el.scrollIntoView({ 
      behavior: 'smooth', 
      block: 'start',
      inline: 'nearest'
    })
    
    // 添加高亮效果
    el.classList.add('highlighted')
    setTimeout(() => el.classList.remove('highlighted'), 2000)
  }
}
</script>

<style scoped>
@import 'https://cdnjs.cloudflare.com/ajax/libs/github-markdown-css/5.5.1/github-markdown.min.css';

.doc-container {
  display: flex;
  height: 100vh;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}
.sidebar {
  width: 240px;
  padding: 1rem;
  background: #f8f9fa;
  border-right: 1px solid #e1e4e8;
  overflow-y: auto;
  box-shadow: 2px 0 5px rgba(0,0,0,0.05);
}
.nav-item {
  font-size: 14px;
  margin-bottom: 8px;
  cursor: pointer;
  padding: 6px 12px;
  border-radius: 4px;
  transition: all 0.2s;
}
.nav-item:hover {
  background-color: #e9ecef;
}
.nav-level-1 {
  font-size: 16px;
  font-weight: 600;
  color: #0969da;
  margin-top: 12px;
}
.content {
  flex: 1;
  padding: 1rem 2rem;
  overflow-y: auto;
  background-color: #fff;
}
.toolbar {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eaecef;
}
.toggle-btn,
.save-btn {
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
  transition: background-color 0.2s;
}
.toggle-btn {
  background: #0969da;
  color: #fff;
}
.toggle-btn:hover {
  background: #0550ae;
}
.save-btn {
  background: #2da44e;
  color: #fff;
}
.save-btn:hover {
  background: #1f7c3a;
}
.editor {
  width: 100%;
  min-height: calc(100vh - 150px);
  font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
  padding: 16px;
  border-radius: 8px;
  border: 1px solid #d0d7de;
  font-size: 15px;
  line-height: 1.6;
  resize: none;
  box-shadow: inset 0 1px 3px rgba(0,0,0,0.05);
}
.editor:focus {
  outline: none;
  border-color: #0969da;
  box-shadow: 0 0 0 3px rgba(9, 105, 218, 0.1);
}
.preview {
  line-height: 1.6;
  font-size: 16px;
}
.markdown-body {
  padding: 1rem;
  box-sizing: border-box;
}
.error {
  padding: 16px;
  background: #ffebee;
  color: #b71c1c;
  border-radius: 8px;
  margin-bottom: 20px;
  border: 1px solid #ffcdd2;
}
</style>

<style>
/* 全局样式修复 */
.markdown-body h1, 
.markdown-body h2, 
.markdown-body h3, 
.markdown-body h4, 
.markdown-body h5, 
.markdown-body h6 {
  margin-top: 24px;
  margin-bottom: 16px;
  font-weight: 600;
  line-height: 1.25;
}

.markdown-body p {
  margin-bottom: 16px;
}

.markdown-body ul, 
.markdown-body ol {
  padding-left: 2em;
  margin-bottom: 16px;
}

.markdown-body pre {
  background: #f6f8fa;
  padding: 16px;
  border-radius: 6px;
  overflow: auto;
  margin-bottom: 16px;
}

.markdown-body code {
  font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
  background-color: rgba(175,184,193,0.2);
  padding: 0.2em 0.4em;
  border-radius: 6px;
  font-size: 85%;
}

.highlighted {
  animation: highlight 2s ease;
}

@keyframes highlight {
  0% { background-color: rgba(9, 105, 218, 0.3); }
  100% { background-color: transparent; }
}
</style>