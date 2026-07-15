<template>
  <header class="app-header">
    <div class="header-container">
      <!-- Logo -->
      <div class="logo" @click="$router.push('/')">
        <span class="logo-text">个人博客</span>
      </div>

      <!-- 导航菜单 -->
      <nav class="nav-menu">
        <router-link to="/" class="nav-item">首页</router-link>
      </nav>

      <!-- 搜索框 -->
      <div class="search-box">
        <el-input v-model="searchKeyword" placeholder="搜索文章..." @keyup.enter="handleSearch">
          <template #append>
            <el-button :icon="Search" @click="handleSearch" />
          </template>
        </el-input>
      </div>

      <!-- 用户区域 -->
      <div class="user-area">
        <template v-if="isLoggedIn">
          <!-- 发布文章按钮 -->
          <el-button type="primary" class="publish-btn" @click="$router.push('/editor')">
            <el-icon><Edit /></el-icon>
            创作文章
          </el-button>
          
          <el-dropdown @command="handleCommand">
            <div class="user-info">
              <el-avatar :size="32" :src="userInfo.avatar" />
              <span class="username">{{ userInfo.nickname }}</span>
              <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item v-if="userStore.isAdmin" command="admin">
                  <el-icon><Tools /></el-icon>管理系统
                </el-dropdown-item>
                <el-dropdown-item command="space">
                  <el-icon><User /></el-icon>我的空间
                </el-dropdown-item>
                <el-dropdown-item command="center">
                  <el-icon><Setting /></el-icon>个人中心
                </el-dropdown-item>
                <el-dropdown-item command="wallet">
                  <el-icon><Wallet /></el-icon>我的钱包
                </el-dropdown-item>
                <el-dropdown-item command="messages">
                  <el-icon><ChatDotRound /></el-icon>私信
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <template v-else>
          <el-button type="primary" @click="$router.push('/login')">登录</el-button>
          <el-button @click="$router.push('/register')">注册</el-button>
        </template>
      </div>
    </div>
  </header>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { Search, Edit, ArrowDown, User, Setting, Wallet, ChatDotRound, SwitchButton, Tools } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const searchKeyword = ref('')
const isLoggedIn = computed(() => !!userStore.token)
const userInfo = computed(() => userStore.userInfo || {})

const handleSearch = () => {
  if (searchKeyword.value.trim()) {
    router.push({ path: '/search', query: { q: searchKeyword.value } })
  }
}

const handleCommand = (command) => {
  const routes = {
    admin: '/admin',
    space: `/space/${userInfo.value.id}`,
    center: '/user-center',
    wallet: '/wallet',
    messages: '/messages'
  }
  if (command === 'logout') {
    userStore.logout()
    router.push('/')
  } else {
    router.push(routes[command])
  }
}
</script>


<style scoped>
.app-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 64px;
  background: linear-gradient(to right, #ffffff, #fff1f2);
  box-shadow: 0 1px 8px rgba(244, 63, 94, 0.1);
  z-index: 1000;
  border-bottom: 1px solid #fecdd3;
}

.header-container {
  max-width: 1200px;
  margin: 0 auto;
  height: 100%;
  display: flex;
  align-items: center;
  padding: 0 20px;
  gap: 30px;
}

.logo {
  cursor: pointer;
}

.logo-text {
  font-size: 22px;
  font-weight: 700;
  background: linear-gradient(135deg, #f43f5e, #ec4899);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.nav-menu {
  display: flex;
  gap: 20px;
}

.nav-item {
  color: #4b5563;
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  transition: color 0.2s;
}

.nav-item:hover,
.nav-item.router-link-active {
  color: #f43f5e;
}

.search-box {
  flex: 1;
  max-width: 360px;
}

.user-area {
  display: flex;
  align-items: center;
  gap: 14px;
}

.publish-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  border-radius: 20px;
  padding: 8px 18px;
  font-weight: 500;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 6px 12px;
  border-radius: 20px;
  transition: background 0.2s;
}

.user-info:hover {
  background: #ffe4e6;
}

.username {
  font-size: 14px;
  color: #1f2937;
  font-weight: 500;
}

.dropdown-icon {
  font-size: 12px;
  color: #9ca3af;
  transition: transform 0.2s;
}

.user-info:hover .dropdown-icon {
  transform: rotate(180deg);
}

:deep(.el-dropdown-menu__item) {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
}

:deep(.el-dropdown-menu__item .el-icon) {
  font-size: 16px;
  color: #4b5563;
}

:deep(.el-dropdown-menu__item:hover .el-icon) {
  color: #f43f5e;
}
</style>
