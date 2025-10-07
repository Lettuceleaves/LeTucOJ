// main.js 或 main.ts
import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'

// 注册全局 dialog
import Dialog from './components/dialog/dialog.js'

const app = createApp(App)
app.use(router)
app.use(ElementPlus)
app.config.globalProperties.$dialog = Dialog

app.mount('#app')
app.config.globalProperties.$ip = "localhost"

document.title = 'LetucOJ';

function setEmojiFavicon(emoji) {
    // 使用 SVG 封装 Emoji
    const svg = `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><text y=".9em" font-size="90">${emoji}</text></svg>`;
    // 将 SVG 编码为 Data URI
    const dataUri = `data:image/svg+xml,${encodeURIComponent(svg)}`;
    
    // 查找或创建 link 标签
    let link = document.querySelector("link[rel*='icon']") || document.createElement('link');
    link.type = 'image/svg+xml';
    link.rel = 'icon';
    link.href = dataUri;
    document.getElementsByTagName('head')[0].appendChild(link);
}

setEmojiFavicon('😇');

/* ---------- 全局 fetch 拦截 ---------- */
const IGNORED_PATHNAMES = [
    '/code.txt'
];

;(function () {
  const _originFetch = window.fetch
  window.fetch = async function (...args) {

    const input = args[0];
    const requestUrl = (typeof input === 'string') ? input : input?.url || ''; 
    const shouldIgnore = IGNORED_PATHNAMES.includes(requestUrl);
    if (shouldIgnore) {
      return await _originFetch(...args);
    }

    const res = await _originFetch(...args)

    const cloned = res.clone()
    
    const data = await cloned.json();

    if (data.code === "A010003") {
      localStorage.removeItem("jwt")
      router.push("/login")
    }

    if (data.token) {
      localStorage.setItem('jwt', data.token);
    }

    return res
  }
})()
