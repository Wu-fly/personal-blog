/**
 * 存储工具类
 * 使用 sessionStorage 实现多标签页独立登录
 */

// 用户认证相关的存储键
const AUTH_KEYS = ['token', 'userInfo']

// 用户数据相关的存储键（按用户ID隔离，使用 sessionStorage）
const USER_DATA_KEYS = [
  'likedArticles',
  'collectedArticles', 
  'browseHistory'
]

// 购买记录（使用 localStorage 全局共享，因为购买是永久性的）
const PURCHASE_KEYS = ['purchasedArticles']

// 应用配置相关的存储键（可以使用 localStorage 共享）
const CONFIG_KEYS = [
  'filterCategory',
  'sortBy',
  'customTags',
  'customCategories'
]

/**
 * 获取存储项
 * @param {string} key - 存储键
 * @returns {string|null} 存储值
 */
export function getItem(key) {
  // 认证信息和用户数据使用 sessionStorage（标签页独立）
  if (AUTH_KEYS.includes(key) || USER_DATA_KEYS.includes(key)) {
    return sessionStorage.getItem(key)
  }
  // 应用配置使用 localStorage（全局共享）
  if (CONFIG_KEYS.includes(key)) {
    return localStorage.getItem(key)
  }
  // 默认使用 sessionStorage
  return sessionStorage.getItem(key)
}

/**
 * 设置存储项
 * @param {string} key - 存储键
 * @param {string} value - 存储值
 */
export function setItem(key, value) {
  // 认证信息和用户数据使用 sessionStorage（标签页独立）
  if (AUTH_KEYS.includes(key) || USER_DATA_KEYS.includes(key)) {
    sessionStorage.setItem(key, value)
    return
  }
  // 应用配置使用 localStorage（全局共享）
  if (CONFIG_KEYS.includes(key)) {
    localStorage.setItem(key, value)
    return
  }
  // 默认使用 sessionStorage
  sessionStorage.setItem(key, value)
}

/**
 * 移除存储项
 * @param {string} key - 存储键
 */
export function removeItem(key) {
  // 认证信息和用户数据使用 sessionStorage
  if (AUTH_KEYS.includes(key) || USER_DATA_KEYS.includes(key)) {
    sessionStorage.removeItem(key)
    return
  }
  // 应用配置使用 localStorage
  if (CONFIG_KEYS.includes(key)) {
    localStorage.removeItem(key)
    return
  }
  // 默认使用 sessionStorage
  sessionStorage.removeItem(key)
}

/**
 * 清除所有认证信息
 */
export function clearAuth() {
  AUTH_KEYS.forEach(key => {
    sessionStorage.removeItem(key)
  })
}

/**
 * 清除所有用户数据
 */
export function clearUserData() {
  USER_DATA_KEYS.forEach(key => {
    sessionStorage.removeItem(key)
  })
}

export default {
  getItem,
  setItem,
  removeItem,
  clearAuth,
  clearUserData
}
