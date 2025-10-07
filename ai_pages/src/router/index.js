import { createRouter, createWebHashHistory } from 'vue-router';

const router = createRouter({
  history: createWebHashHistory(),
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('../views/HomeView.vue'),
    },
    {
      path: '/login',
      name: 'Login',
      component: () => import('../views/Login.vue'),
    },
    {
      path: '/register',
      name: 'Register',
      component: () => import('../views/Register.vue'),
    },
    {
      path: '/editor/:name',
      name: 'Editor',
      component: () => import('../views/Editor.vue'),
      props: true,
    },
    {
      path: '/form',
      name: 'form',
      component: () => import('../views/ProblemForm.vue'),
    },
    {
      path: '/docs',
      name: 'docs',
      component: () => import('../views/DocPage.vue'),
    },
    {
      path: '/main',
      name: 'main',
      component: () => import('../views/Main.vue'),
    },
    {
      path: '/contest',
      name: 'contest',
      component: () => import('../views/MainPages/Contest.vue'),
    },
    {
      path: '/contest/detail',
      name: 'contestDetail',
      component: () => import('../views/MainPages/ContestPages/ContestDetail.vue'),
      props: true,
    },
    {
      path: '/contest/form',
      name: 'contestForm',
      component: () => import('../views/MainPages/ContestPages/ContestForm.vue'),
      props: true,
    },
    {
      path: '/contest/editor',
      name: 'contestEditor',
      component: () => import('../views/MainPages/ContestPages/ContestEditor.vue'),
    },
    {
      path: '/user',
      name: 'user',
      component: () => import('../views/ManagePage.vue'),
    },
    {
      path: '/admin',
      name: 'AdminDashboard',
      component: () => import('../views/ManagePage.vue'),
    },
    {
      path: '/admin/users',
      name: 'UserList',
      component: () => import('../views/ManagePages/UserList.vue'),
    },
    {
      path: '/admin/db',
      name: 'DBSQLRefresh',
      component: () => import('../views/ManagePages/DBSQLRefresh.vue'),
    },
    {
      path: '/admin/history',
      name: 'history',
      component: () => import('../views/ManagePages/History.vue'),
    },
    {
      path: '/othsers/profile',
      name: 'othersProfile',
      component: () => import('../views/MainPages/User.vue'),
      props: true
    }
  ],
});

export default router;
