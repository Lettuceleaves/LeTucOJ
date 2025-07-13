<template>
  <div class="online-editor">
    <!-- 标题栏 -->
    <div class="title-bar">
      <div class="title-item" :class="{ active: activeTab === 'question' }" @click="activeTab = 'question'">答题</div>
      <div class="title-item" :class="{ active: activeTab === 'description' }" @click="activeTab = 'description'">描述</div>
      <div class="title-item" :class="{ active: activeTab === 'solution' }" @click="activeTab = 'solution'">题解</div>
      <div class="title-item" :class="{ active: activeTab === 'result' }" @click="activeTab = 'result'">结果</div>
    </div>

    <!-- 内容区域 -->
    <div class="content">
      <div v-if="activeTab === 'question'">
        <CCodeEditor ref="codeEditorRef" @change="saveCode" />

        <!-- 悬浮按钮区域（仅在答题子页面显示） -->
        <div class="floating-buttons">
          <button @click="goToLogin">退出</button>
          <button @click="submitCode">提交</button>
          <button @click="checkCode">检测</button>
        </div>
      </div>
      <div v-else-if="activeTab === 'description'">
        <p>这里是题目描述内容</p>
      </div>
      <div v-else-if="activeTab === 'solution'">
        <p>这里是题解内容</p>
      </div>
      <div v-else-if="activeTab === 'result'">
        <ResultPanel :result="result" />
      </div>
    </div>
  </div>

  <input v-model="inputValue" placeholder="随便输入点什么..." />

</template>

<script setup>
import { ref, watch, getCurrentInstance, onMounted, onBeforeUnmount, nextTick } from 'vue';
import { useRouter } from 'vue-router';
import CCodeEditor from '../components/CCodeEditor.vue';
import ResultPanel from './EditorSubPages/ResultPanel.vue' 

const codeEditorRef = ref(null);
const router = useRouter();
const activeTab = ref('question'); // 默认显示答题页
const result = ref(null); // 用于存储提交或检测的结果
const { proxy } = getCurrentInstance()
const inputValue = ref('')

// 全局键盘监听器
function handleKeydown(event) {
  const key = event.key
  const lastChar = inputValue.value.slice(-1)

  console.log('按键:', key)
  console.log('当前值:', inputValue.value)

  if (
    key === ' ' ||           // 空格
    key === 'Enter' ||       // 回车
    lastChar === 'x'         // 上一个字符是 x
  ) {
    
    const code = codeEditorRef.value?.getCode(); // 获取代码

    if (!code) {
      return;
    }
    localStorage.setItem('userCode', code);
  }
}

watch(activeTab, (newVal) => {
  if (newVal === 'question') {
    const savedCode = localStorage.getItem('userCode') || '';
    nextTick(() => {
      codeEditorRef.value?.setCode(savedCode);
    });
  }
});

onMounted(() => {
  codeEditorRef.value?.setCode(localStorage.getItem('userCode'));
  window.addEventListener('keydown', handleKeydown);
})

onBeforeUnmount(() => {
  window.removeEventListener('keydown', handleKeydown);
})

// 跳转到登录页面
const goToLogin = () => {
  router.push('/login');
};

// 提交代码函数
const sendCode = async (action) => {

// const ok = await proxy.$dialog.confirm('你确定要提交这份代码吗？')

//   if (ok) {
//     console.log('开始执行提交逻辑...')
//   } else {
//     console.log('用户取消了提交')
//   }

  const code = codeEditorRef.value?.getCode(); // 获取代码

  if (!code) {
    alert('无法获取代码内容');
    return;
  }
  
    activeTab.value = 'result'; // 跳转到结果子页面

  try {
    const response = await fetch('http://127.0.0.1:2222/practice/submitTest', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        service: 0, // 0: run
        code: code, // 用户输入的代码
        problemId: 'test', // 固定值
      }),
    });

    const resultData = await response.json();
    result.value = `提交成功！结果：${JSON.stringify(resultData)}`;
  } catch (error) {
    result.value = `提交失败！错误信息：${error.message}`;
  }
};

const submitCode = () => sendCode('提交');
const checkCode = () => sendCode('检测');

</script>

<style>
.online-editor {
  height: 100vh;
  width: 100vw;
  padding: 0;
  margin: 0;
  box-sizing: border-box;
  --editor-background-color: #1e1e1e;
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
  padding: 20px;
  height: calc(100% - 60px); /* 调整高度以适应标题栏 */
  overflow-y: auto;
}

.CodeMirror {
  border: var(--editor-background-color);
  height: 100%;
}

.CodeMirror-scroll {
  height: 100vh;
  overflow-y: hidden;
  overflow-x: auto;
  background-color: var(--editor-background-color);
}

.CodeMirror-gutter {
  background-color: var(--editor-background-color);
}

/* 悬浮按钮样式 */
.floating-buttons {
  position: absolute;
  top: 20px;
  right: 20px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  z-index: 1000;
}

.floating-buttons button {
  padding: 8px 12px;
  background-color: #3b82f6;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: bold;
  transition: background-color 0.2s;
}

.floating-buttons button:hover {
  background-color: #2563eb;
}
</style>
