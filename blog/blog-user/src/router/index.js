import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'

// 静态导入所有视图组件（避免动态导入的缓存问题）
import Home from '@/views/Home.vue'
import Login from '@/views/Login.vue'
import Register from '@/views/Register.vue'
import ArticleDetail from '@/views/ArticleDetail.vue'
import SearchResult from '@/views/SearchResult.vue'
import UserCenter from '@/views/UserCenter.vue'
import Wallet from '@/views/Wallet.vue'
import Messages from '@/views/Messages.vue'
import PersonalSpace from '@/views/PersonalSpace.vue'
import SpaceSettings from '@/views/SpaceSettings.vue'
import Admin from '@/views/Admin.vue'
import ArticleEditor from '@/views/ArticleEditor.vue'

// 路由配置
const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home,
    meta: { title: '首页' }
  },
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: { title: '登录', guest: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: Register,
    meta: { title: '注册', guest: true }
  },
  {
    path: '/article/:id',
    name: 'ArticleDetail',
    component: ArticleDetail,
    meta: { title: '文章详情' }
  },
  {
    path: '/search',
    name: 'SearchResult',
    component: SearchResult,
    meta: { title: '搜索结果' }
  },
  {
    path: '/user-center',
    name: 'UserCenter',
    component: UserCenter,
    meta: { title: '个人中心', requiresAuth: true }
  },
  {
    path: '/wallet',
    name: 'Wallet',
    component: Wallet,
    meta: { title: '我的钱包', requiresAuth: true }
  },
  {
    path: '/messages',
    name: 'Messages',
    component: Messages,
    meta: { title: '私信', requiresAuth: true }
  },
  {
    path: '/space/:id',
    name: 'PersonalSpace',
    component: PersonalSpace,
    meta: { title: '个人空间' }
  },
  {
    path: '/space-settings',
    name: 'SpaceSettings',
    component: SpaceSettings,
    meta: { title: '空间设置', requiresAuth: true, requiresBlogger: true }
  },
  {
    path: '/admin',
    name: 'Admin',
    component: Admin,
    meta: { title: '管理系统', requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/editor',
    name: 'ArticleEditor',
    component: ArticleEditor,
    meta: { title: '写文章', requiresAuth: true, requiresBlogger: true }
  },
  {
    path: '/article-editor/:id',
    name: 'ArticleEditorEdit',
    component: ArticleEditor,
    meta: { title: '编辑文章', requiresAuth: true, requiresBlogger: true }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: Home,
    meta: { title: '页面不存在' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    // 返回保存的位置或滚动到顶部
    if (savedPosition) {
      return savedPosition
    } else {
      return { top: 0 }
    }
  }
})

// 路由守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - 个人博客` : '个人博客'
  
  // 获取用户状态（从 sessionStorage 读取，避免循环依赖）
  const token = sessionStorage.getItem('token')
  const userInfo = JSON.parse(sessionStorage.getItem('userInfo') || 'null')
  const isLoggedIn = !!token
  const isBlogger = userInfo?.role === 'BLOGGER' || userInfo?.role === 'ADMIN'
  const isAdmin = userInfo?.role === 'ADMIN'
  
  // 访客页面（登录/注册）- 已登录用户跳转首页
  if (to.meta.guest && isLoggedIn) {
    next({ path: '/' })
    return
  }
  
  // 需要登录的页面
  if (to.meta.requiresAuth && !isLoggedIn) {
    ElMessage.warning('请先登录')
    next({ 
      path: '/login', 
      query: { redirect: to.fullPath } 
    })
    return
  }
  
  // 需要管理员权限的页面
  if (to.meta.requiresAdmin && !isAdmin) {
    ElMessage.warning('需要管理员权限')
    next({ path: '/' })
    return
  }
  
  // 需要博主权限的页面
  if (to.meta.requiresBlogger && !isBlogger) {
    ElMessage.warning('需要博主权限')
    next({ path: '/user-center' })
    return
  }
  
  next()
})

// 路由后置守卫
router.afterEach((to, from) => {
  // 可以在这里添加页面访问统计等
})

export default router
