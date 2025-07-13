import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import Login from '../views/Login.vue';
import Register from '../views/Register.vue';
import Editor from '../views/Editor.vue';
import ProblemForm from '../views/ProblemForm.vue';
import 'codemirror';

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
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
      path: '/list',
      name: 'List',
      component: () => import('../views/List.vue'),
    },
    {
      path: '/problem-form',
      name: 'problem-form',
      component: ProblemForm,
    }
  ],
})

export default router

