import { createRouter, createWebHashHistory } from 'vue-router'
import HomePage from '@/views/HomePage.vue'
import LoginPage from '@/views/LoginPage.vue'
import RegisterPage from '@/views/RegisterPage.vue'
import EditorPage from '@/views/EditorPage.vue'
import ProblemForm from '@/views/ProblemForm.vue'
import DocPage from '@/views/DocPage.vue'
import PracticeListPage from '@/views/PracticeListPage.vue'
import HistoryPage from '@/views/HistoryPage.vue'
import ContestPage from '@/views/ContestMain.vue'

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
      name: 'login',
      component: LoginPage,
    },
    {
      path: '/register',
      name: 'register',
      component: RegisterPage,
    },
    {
      path: '/editor/:name',
      name: 'editor',
      component: EditorPage,
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
      path: '/practices',
      name: 'practices',
      component: PracticeListPage,
    },
    {
      path: '/contests',
      name: 'contest',
      component: ContestPage,
    },
  ],
})

export default router
