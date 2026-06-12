import { createRouter, createWebHistory } from 'vue-router'
import Layout from '@/layout/index.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '首页', icon: 'House', requiresAuth: true }
      }
    ]
  },
  {
    path: '/bill',
    component: Layout,
    children: [
      {
        path: '',
        name: 'Bill',
        component: () => import('@/views/bill/index.vue'),
        meta: { title: '账单', icon: 'Document', requiresAuth: true }
      }
    ]
  },
  {
    path: '/subscription',
    component: Layout,
    children: [
      {
        path: '',
        name: 'Subscription',
        component: () => import('@/views/subscription/index.vue'),
        meta: { title: '订阅管家', icon: 'Bell', requiresAuth: true }
      }
    ]
  },
  {
    path: '/wish',
    component: Layout,
    children: [
      {
        path: '',
        name: 'Wish',
        component: () => import('@/views/wish/index.vue'),
        meta: { title: '愿望清单', icon: 'Star', requiresAuth: true }
      }
    ]
  },
  {
    path: '/share',
    component: Layout,
    children: [
      {
        path: '',
        name: 'Share',
        component: () => import('@/views/share/index.vue'),
        meta: { title: 'AA共享', icon: 'Share', requiresAuth: true }
      }
    ]
  },
  {
    path: '/report',
    component: Layout,
    children: [
      {
        path: '',
        name: 'Report',
        component: () => import('@/views/report/index.vue'),
        meta: { title: '报告', icon: 'DataAnalysis', requiresAuth: true }
      }
    ]
  },
  {
    path: '/chart',
    component: Layout,
    children: [
      {
        path: '',
        name: 'Chart',
        component: () => import('@/views/chart/index.vue'),
        meta: { title: '图表', icon: 'TrendCharts', requiresAuth: true }
      }
    ]
  },
  {
    path: '/budget',
    component: Layout,
    children: [
      {
        path: '',
        name: 'Budget',
        component: () => import('@/views/budget/index.vue'),
        meta: { title: '预算', icon: 'Management', requiresAuth: true }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  
  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else if (to.path === '/login' && token) {
    next('/dashboard')
  } else {
    next()
  }
})

export default router
