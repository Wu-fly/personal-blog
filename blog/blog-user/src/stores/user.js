import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getItem, setItem, removeItem, clearAuth } from '@/utils/storage'

export const useUserStore = defineStore('user', () => {
  // 状态 - 使用 sessionStorage 实现多标签页独立登录
  const token = ref(getItem('token') || '')
  const userInfo = ref(JSON.parse(getItem('userInfo') || 'null'))
  
  // 计算属性
  const isLoggedIn = computed(() => !!token.value)
  const isBlogger = computed(() => userInfo.value?.role === 'BLOGGER')
  const isAdmin = computed(() => userInfo.value?.role === 'ADMIN')
  
  // 方法
  function setToken(newToken) {
    token.value = newToken
    setItem('token', newToken)
  }
  
  function setUserInfo(info) {
    userInfo.value = info
    setItem('userInfo', JSON.stringify(info))
  }
  
  function logout() {
    token.value = ''
    userInfo.value = null
    clearAuth()
  }
  
  function updateUserInfo(updates) {
    if (userInfo.value) {
      userInfo.value = { ...userInfo.value, ...updates }
      setItem('userInfo', JSON.stringify(userInfo.value))
    }
  }
  
  return {
    token,
    userInfo,
    isLoggedIn,
    isBlogger,
    isAdmin,
    setToken,
    setUserInfo,
    logout,
    updateUserInfo
  }
})
