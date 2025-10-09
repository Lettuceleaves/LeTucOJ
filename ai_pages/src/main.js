import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import Dialog from './components/dialog/dialog.js'

const app = createApp(App)
app.use(router)
app.use(ElementPlus)
app.config.globalProperties.$dialog = Dialog

app.mount('#app')
app.config.globalProperties.$ip = "localhost"

document.title = 'LetucOJ';

function setEmojiFavicon(emoji) {

    const svg = `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><text y=".9em" font-size="90">${emoji}</text></svg>`;

    const dataUri = `data:image/svg+xml,${encodeURIComponent(svg)}`;

    let link = document.querySelector("link[rel*='icon']") || document.createElement('link');
    link.type = 'image/svg+xml';
    link.rel = 'icon';
    link.href = dataUri;
    document.getElementsByTagName('head')[0].appendChild(link);
}

setEmojiFavicon('😇');

/* ---------- 全局 fetch 拦截 ---------- */
const IGNORED_PATHNAMES = [
    '/code.txt', 
    '/advice'
];

;(function () {
  const _originFetch = window.fetch
  window.fetch = async function (...args) {

    const input = args[0];
    const requestUrl = (typeof input === 'string') ? input : input?.url || ''; 
    let pathnameToMatch = '';
    
    try {
        if (requestUrl.startsWith('http')) {
            pathnameToMatch = new URL(requestUrl).pathname;
        }
        else if (requestUrl.startsWith('/')) {
            pathnameToMatch = requestUrl;
        }
    } catch (e) {
        pathnameToMatch = requestUrl;
    }
    
    const shouldIgnore = IGNORED_PATHNAMES.includes(pathnameToMatch);
    
    if (shouldIgnore) {
      return await _originFetch(...args);
    }
    const res = await _originFetch(...args)
    const cloned = res.clone()
    
    try {
        const data = await cloned.json();

        if (data.code === "A010003") {
            localStorage.removeItem("jwt")
            if (typeof router !== 'undefined') { 
                router.push("/login")
            }
        }

        if (data.token) {
            localStorage.setItem('jwt', data.token);
        }
    } catch (e) {
    }

    return res
  }
})()
