import { createRouter, createWebHashHistory } from 'vue-router'
import HomePage from '@/views/HomePage.vue'
import LoginPage from '@/views/LoginPage.vue'
import RegisterPage from '@/views/RegisterPage.vue'
import Editor from '@/views/Editor.vue'
import ProblemForm from '@/views/ProblemForm.vue'
import DocPage from '@/views/DocPage.vue'
import MainPage from '@/views/MainPage.vue'
import HistoryPage from '@/views/HistoryPage.vue'
import ContestMain from '@/views/ContestMain.vue'

const router = createRouter({
  history: createWebHashHistory(),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomePage,
    },
    {
      path: '/login',
      name: 'Login',
      component: LoginPage,
    },
    {
      path: '/register',
      name: 'Register',
      component: RegisterPage,
    },
    {
      path: '/editor/:name',
      name: 'Editor',
      component: Editor,
      props: true, // ✅ 让 name 参数传给组件
    },
    {
      path: '/form',
      name: 'form',
      component: ProblemForm,
    },
    {
      path: '/docs',
      name: 'docs',
      component: DocPage,
    },
    {
      path: '/history',
      name: 'history',
      component: HistoryPage,
    },
    {
      path: '/main',
      name: 'main',
      component: MainPage,
    },
    {
      path: '/contest',
      name: 'contest',
      component: ContestMain,
    },
  ],
})

export default router
