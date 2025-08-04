import { createRouter, createWebHashHistory } from 'vue-router'
import HomePage from '@/views/HomePage.vue'
import LoginPage from '@/views/LoginPage.vue'
import RegisterPage from '@/views/RegisterPage.vue'
import ListPage from '@/views/MainPages/ListPage.vue'
import Editor from '@/views/Editor.vue'
import ProblemForm from '@/views/ProblemForm.vue'
import DocPage from '@/views/DocPage.vue'

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
      path: '/list',
      name: 'list',
      component: ListPage,
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
      component: () => import('../views/History.vue'),
    },
    {
      path: '/main',
      name: 'main',
      component: () => import('../views/Main.vue'),
    },
    {
      path: '/contest',
      name: 'contest',
      component: () => import('../views/ContestMain.vue'),
    },
  ],
})

export default router
