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
