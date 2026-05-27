import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/home',
    },
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/LoginView.vue'),
      meta: { guest: true },
    },
    {
      path: '/register',
      name: 'Register',
      component: () => import('@/views/RegisterView.vue'),
      meta: { guest: true },
    },
    {
      path: '/forgot-password',
      name: 'ForgotPassword',
      component: () => import('@/views/ForgotPasswordView.vue'),
      meta: { guest: true },
    },
    {
      path: '/',
      component: () => import('@/layouts/MainLayout.vue'),
      children: [
        {
          path: 'home',
          name: 'Home',
          component: () => import('@/views/HomeView.vue'),
        },
        {
          path: 'hardware',
          name: 'Hardware',
          component: () => import('@/views/HardwareView.vue'),
        },
        {
          path: 'hardware/:id',
          name: 'HardwareDetail',
          component: () => import('@/views/HardwareDetailView.vue'),
        },
        {
          path: 'hardware-detail',
          name: 'HardwareDetailByName',
          component: () => import('@/views/HardwareDetailView.vue'),
        },
        {
          path: 'ranking',
          name: 'Ranking',
          component: () => import('@/views/RankingView.vue'),
        },
        {
          path: 'recommend',
          name: 'Recommend',
          component: () => import('@/views/RecommendView.vue'),
        },
        {
          path: 'evaluations',
          name: 'Evaluations',
          component: () => import('@/views/EvaluationsView.vue'),
        },
        {
          path: 'evaluation/:id',
          name: 'EvaluationDetail',
          component: () => import('@/views/EvaluationDetailView.vue'),
        },
        {
          path: 'user',
          name: 'User',
          component: () => import('@/views/UserView.vue'),
          meta: { requiresAuth: true },
        },
      ],
    },
    {
      path: '/admin',
      component: () => import('@/views/admin/AdminLayout.vue'),
      meta: { requiresAdmin: true },
      children: [
        { path: '', name: 'Admin', component: () => import('@/views/admin/AdminDashboard.vue') },
        { path: 'hardware', name: 'AdminHardware', component: () => import('@/views/admin/AdminHardware.vue') },
        { path: 'audit', name: 'AdminAudit', component: () => import('@/views/admin/AdminAudit.vue') },
      ],
    },
  ],
})

// 路由守卫 — 认证和授权
router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('token')
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || 'null')
  const isAdmin = userInfo?.role === 'ADMIN'

  // 管理员只能访问 admin 相关页面和登录页
  if (isAdmin && token && !to.path.startsWith('/admin') && to.path !== '/login') {
    next('/admin')
    return
  }

  if (to.meta.requiresAdmin) {
    if (!token || !isAdmin) {
      next('/home')
      return
    }
  }

  if (to.meta.requiresAuth && !token) {
    next('/login')
    return
  }

  // guest 路由：已登录用户不能访问
  if (token && to.meta.guest) {
    next(isAdmin ? '/admin' : '/home')
    return
  }

  next()
})

export default router
