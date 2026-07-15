import { createPinia } from 'pinia'

// 创建 Pinia 实例
const pinia = createPinia()

// 清理旧的 localStorage 数据（迁移到 sessionStorage）
const cleanupOldStorage = () => {
  const keysToClean = ['token', 'userInfo', 'pinia-user', 'pinia-article']
  keysToClean.forEach(key => {
    if (localStorage.getItem(key)) {
      console.log(`清理旧的 localStorage 数据: ${key}`)
      localStorage.removeItem(key)
    }
  })
}

// 执行清理
cleanupOldStorage()

// 状态持久化插件 - 使用 sessionStorage 实现标签页独立
pinia.use(({ store }) => {
  // 需要持久化的 store
  const persistStores = ['user', 'article']
  
  if (persistStores.includes(store.$id)) {
    // 从 sessionStorage 恢复状态（标签页独立）
    const savedState = sessionStorage.getItem(`pinia-${store.$id}`)
    if (savedState) {
      try {
        store.$patch(JSON.parse(savedState))
      } catch (e) {
        console.error(`Failed to restore state for ${store.$id}:`, e)
      }
    }
    
    // 监听状态变化并保存到 sessionStorage
    store.$subscribe((mutation, state) => {
      sessionStorage.setItem(`pinia-${store.$id}`, JSON.stringify(state))
    })
  }
})

export default pinia

export * from './user'
export * from './article'
export * from './wallet'
