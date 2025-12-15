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

// 不再需要全局fetch拦截器，已在axios实例中配置
