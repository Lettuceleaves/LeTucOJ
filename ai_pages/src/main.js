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
