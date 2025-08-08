import { createApp } from 'vue'
import App from './App.vue'
import router from './router'

// import { useDark } from '@vueuse/core'

// Element Plus
import ElementPlus from 'element-plus'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import 'element-plus/dist/index.css'
import 'element-plus/theme-chalk/dark/css-vars.css'

// UnoCSS
import 'virtual:uno.css'
import './assets/base.css'

// Monaco Editor
import { loader } from '@guolao/vue-monaco-editor'

// Highlight.js 代码高亮
import HighlightJs from '@highlightjs/vue-plugin'
import hljs from 'highlight.js';
import python from 'highlight.js/lib/languages/python'
import 'highlight.js/styles/atom-one-dark.css'

// 注册全局 dialog
import Dialog from './components/dialog/dialog'


const app = createApp(App)

app.use(router)

// Element Plus
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}
app.use(ElementPlus)

// Monaco Editor (CDN)
loader.config({
  paths: {
    vs: 'https://unpkg.com/monaco-editor@0.52.2/min/vs'
  }
})

// Highlight.js
hljs.registerLanguage('python', python)
app.use(HighlightJs)

app.mount('#app')

// 添加全局变量
app.config.globalProperties.$dialog = Dialog
app.config.globalProperties.$ip = "letucoj.cn"
