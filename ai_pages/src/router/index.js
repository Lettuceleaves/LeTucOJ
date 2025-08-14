import { createRouter, createWebHashHistory  } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import Login from '../views/Login.vue';
import Register from '../views/Register.vue';
import Editor from '../views/Editor.vue';
import ProblemForm from '../views/ProblemForm.vue';
import DocPage from '@/views/DocPage.vue'

const router = createRouter({
  history: createWebHashHistory (),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
    },
    {
      path: '/login',
      name: 'Login',
      component: Login,
    },
    {
      path: '/register',
      name: 'Register',
      component: Register,
    },
    {
      path: '/editor/:name',
      name: 'Editor',
      component: Editor,
      props: true // ✅ 让 name 参数传给组件
    },
    {
      path: '/form',
      name: 'form',
      component: ProblemForm,
    },
    { 
      path: '/docs',
      name: 'docs',
      component: DocPage 
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
    }
    ,
    {
      path: '/contest',
      name: 'contest',
      component: () => import('../views/MainPages/Contest.vue'),
    },{
      path: '/contest/detail',
      name: 'contestDetail',
      component: () => import('../views/MainPages/ContestPages/ContestDetail.vue'),
      props: true
    },{
      path: '/contest/form',
      name: 'contestForm',
      component: () => import('../views/MainPages/ContestPages/ContestForm.vue'),
      props: true
    },{
      path: '/contest/editor',
      name: 'contestEditor',
      component: () => import('../views/MainPages/ContestPages/ContestEditor.vue'),
    },{
      path: '/user',
      name: 'user',
      component: () => import('../views/UserPage.vue'),
    }
  ],
})

export default router

