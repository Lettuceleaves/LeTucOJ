import { createRouter, createWebHashHistory } from 'vue-router'
import HomePage from '@/views/HomePage.vue'
import LoginPage from '@/views/LoginPage.vue'
import RegisterPage from '@/views/RegisterPage.vue'
import DocPage from '@/views/DocPage.vue'
import PracticeListPage from '@/views/PracticeListPage.vue'
import ContestPage from '@/views/ContestMain.vue'
import PracticeDetailPage from '@/views/PracticeDetailPage.vue'
import PracticeSubmitPage from '@/views/PracticeSubmitPage.vue'

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
      path: '/docs',
      name: 'docs',
      component: DocPage,
    },
    {
      path: '/practices',
      name: 'practices',
      component: PracticeListPage,
    },
    {
      path: '/practices/:name',
      name: 'detail',
      component: PracticeDetailPage,
    },
    {
      path: '/practices/:name/submit',
      name: 'submit',
      component: PracticeSubmitPage,
    },
    {
      path: '/contests',
      name: 'contest',
      component: ContestPage,
    },
  ],
})

export default router
