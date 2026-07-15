import { createRouter, createWebHistory } from 'vue-router'
import { useAdminStore } from '../stores/admin'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('../layouts/AdminLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        redirect: '/dashboard'
      },
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/Dashboard.vue')
      },
      {
        path: 'articles/review',
        name: 'ArticleReview',
        component: () => import('../views/ArticleReview.vue')
      },
      {
        path: 'users',
        name: 'UserManagement',
        component: () => import('../views/UserManagement.vue')
      },
      {
        path: 'carousel',
        name: 'CarouselManagement',
        component: () => import('../views/CarouselManagement.vue')
      },
      {
        path: 'blogger-applications',
        name: 'BloggerApplications',
        component: () => import('../views/BloggerApplications.vue')
      },
      {
        path: 'wallet',
        name: 'AdminWallet',
        component: () => import('../views/AdminWallet.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from) => {
  const adminStore = useAdminStore()
  
  if (to.meta.requiresAuth && !adminStore.isLoggedIn) {
    return '/login'
  } else if (to.path === '/login' && adminStore.isLoggedIn) {
    return '/dashboard'
  }
})

export default router
