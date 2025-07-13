<script setup>
import { ref, onMounted } from 'vue';

const lines = ref([]);
const visibleLines = ref([]);
let currentIndex = 0;
let scrollInterval = null;

const fetchLines = async () => {
  try {
    const response = await fetch('/code.txt');
    if (!response.ok) throw new Error('Failed to fetch data');
    const text = await response.text();
    lines.value = text.split('\n');
    prefillLines(); // 新增：预先填充行
    startLineDisplay();
  } catch (error) {
    console.error('Error fetching lines:', error);
    lines.value = ['Failed to load lines. Please check the URL and try again.'];
    startLineDisplay();
  }
};

// 新增：预先填充足够多的行以填满屏幕
const prefillLines = () => {
  const linesPerScreen = Math.floor(window.innerHeight / 300); // 估算每屏能显示的行数
  visibleLines.value = Array(linesPerScreen).fill('').map((_, i) => 
    i < lines.value.length ? lines.value[i] : ''
  );
  currentIndex = Math.min(linesPerScreen, lines.value.length);
};

const startLineDisplay = () => {
  // 清除已有定时器
  if (scrollInterval) clearInterval(scrollInterval);
  
  // 重置状态
  visibleLines.value = [];
  currentIndex = 0;
  
  // 每秒添加一行
  scrollInterval = setInterval(() => {
    if (currentIndex < lines.value.length) {
      // 新行添加到顶部，保留最近40行
      visibleLines.value = [lines.value[currentIndex], ...visibleLines.value.slice(0, 300)];
      currentIndex++;
    } else {
      // 当显示完所有行后，重置索引重新开始
      currentIndex = 0;
    }
  }, 30); // 1000ms = 1秒
};

// 组件卸载时清除定时器
onMounted(() => {
  fetchLines();
  return () => {
    if (scrollInterval) clearInterval(scrollInterval);
  };
});
</script>

<template>
  <main class="oj-container">
    <div class="title-container">
      <h1 class="title">LetucOJ</h1>
      <div class="title-border"></div>
    </div>
    
    <div class="button-container">
      <router-link to="/register" class="register-button">注册</router-link>
      <router-link to="/login" class="login-button">登录</router-link>
    </div>

    <div class="doc-link">
      <a href="https://example.com/docs" target="_blank">文档</a>
    </div>
    
    <div class="text-terminal">
      <div
        v-for="(line, index) in visibleLines"
        :key="index"
        class="terminal-line"
        :style="{ top: `${index * 1.5}em` }"
      >
        {{ line }}
      </div>
    </div>
  </main>
</template>

<style scoped>
.oj-container {
  position: relative;
  width: 100vw;
  height: 100vh;
  background-color: black;
  color: darkgreen;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.title-container {
  position: relative;
  margin-bottom: 3rem;
  z-index: 10;
}

.title {
  font-size: 4rem;
  font-weight: bold;
  color: white;
  text-shadow: 
    0 0 10px #00ff00,
    0 0 20px #00ff00;
  padding: 1rem 2rem;
  position: relative;
  z-index: 2;
}

.title-border {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  border: 3px solid darkgreen;
  border-radius: 10px;
  box-shadow: 
    0 0 15px #00ff00,
    inset 0 0 15px #00ff00;
  z-index: 1;
  animation: border-pulse 3s infinite alternate;
  /* 新增半透明背景 */
  background-color: rgba(0, 50, 0, 0.3); /* 半透明深绿色背景 */
  /* 可选：添加背景模糊效果 */
  backdrop-filter: blur(2px);
}

@keyframes border-pulse {
  0% {
    border-color: darkgreen;
    box-shadow: 
      0 0 10px #00ff00,
      inset 0 0 10px #00ff00;
  }
  100% {
    border-color: limegreen;
    box-shadow: 
      0 0 25px #00ff00,
      inset 0 0 25px #00ff00;
  }
}

.button-container {
  display: flex;
  gap: 2rem;
  margin-bottom: 2rem;
  z-index: 10;
}

.register-button, .login-button {
  padding: 0.8rem 2rem;
  font-size: 1.2rem;
  font-weight: bold;
  background-color: rgba(0, 100, 0, 0.7);
  color: white;
  border: 2px solid limegreen;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  z-index: 10;
  text-decoration: none; /* 添加此行 */
}

.register-button:hover, .login-button:hover {
  background-color: rgba(50, 205, 50, 0.8);
  transform: translateY(-3px);
  box-shadow: 0 5px 15px rgba(0, 255, 0, 0.4);
}

.doc-link {
  position: absolute;
  top: 0.5rem;
  right: 2.0rem;
  z-index: 10;
}

.doc-link a {
  display: inline-block;
  padding: 0.8rem 1.5rem;
  background-color: rgb(12, 69, 40);
  color: rgb(255, 255, 255);
  font-weight: bold;
  text-decoration: none;
  border-radius: 25px;
  box-shadow: 0 4px 8px rgba(255, 255, 255, 0.3);
  transition: all 0.3s ease;
}

.doc-link a:hover {
  background-color: #f0f0f0;
  transform: translateY(-2px);
  box-shadow: 0 6px 12px rgba(255, 255, 255, 0.4);
}

.text-terminal {
  position: absolute;
  top: 0;
  left: 10px;
  width: 100%;
  height: 100%;
  padding-top: 20px;
  pointer-events: none;
  overflow: hidden;
  font-family: 'Courier New', monospace;
  z-index: 1;
}

.terminal-line {
  position: absolute;
  left: 0;
  width: 100%;
  color: rgba(50, 205, 50, 0.7);
  font-size: 2rem;
  white-space: pre;
  opacity: 0.8;
  text-align: left;
  text-shadow: 0 0 5px rgba(0, 255, 0, 0.5);
  transition: top 1s step-end;
}
</style>