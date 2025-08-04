import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import ElementPlus from 'element-plus'
// import { useDark } from '@vueuse/core'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import './assets/base.css'
import 'element-plus/dist/index.css'
import 'element-plus/theme-chalk/dark/css-vars.css'
import 'virtual:uno.css'

// 注册 Highlight.js 代码高亮
import HighlightJs from '@highlightjs/vue-plugin'
import hljs from 'highlight.js';
import python from 'highlight.js/lib/languages/python'
import 'highlight.js/styles/atom-one-dark.css'

// 注册全局 dialog
import Dialog from './components/dialog/dialog.js'

const app = createApp(App)

hljs.registerLanguage('python', python)
// 注册 Element Plus 图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(router)
app.use(ElementPlus)
app.use(HighlightJs)
app.config.globalProperties.$dialog = Dialog

app.mount('#app')
app.config.globalProperties.$ip = "letucoj.cn"  // 添加全局变量
