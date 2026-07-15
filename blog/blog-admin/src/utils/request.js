import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useAdminStore } from '../stores/admin'
import router from '../router'

// 创建axios实例
const request = axios.create({
  baseURL: '/api',  // 使用相对路径，通过Vite代理
  timeout: 10000
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    const adminStore = useAdminStore()
    if (adminStore.token) {
      config.headers['Authorization'] = `Bearer ${adminStore.token}`
    }
    return config
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    const res = response.data
    
    // 检查响应格式：支持 {success: true, data: ...} 或 {code: 200, data: ...}
    if (res.success === false || (res.code !== undefined && res.code !== 200 && res.code !== 0)) {
      ElMessage.error(res.message || '请求失败')
      
      // 401: 未授权，跳转到登录页
      if (res.code === 401) {
        const adminStore = useAdminStore()
        adminStore.logout()
        router.push('/login')
      }
      
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    
    return res
  },
  error => {
    console.error('响应错误:', error)
    
    if (error.response) {
      const { status, data } = error.response
      
      switch (status) {
        case 401:
          ElMessage.error('未授权，请重新登录')
          const adminStore = useAdminStore()
          adminStore.logout()
          router.push('/login')
          break
        case 403:
          ElMessage.error('拒绝访问')
          break
        case 404:
          // 404错误不显示提示，让组件自己处理
          console.warn('请求的资源不存在:', error.config.url)
          break
        case 500:
          ElMessage.error('服务器错误')
          break
        default:
          ElMessage.error(data.message || '请求失败')
      }
    } else if (error.request) {
      ElMessage.error('网络错误，请检查网络连接')
    } else {
      ElMessage.error('请求配置错误')
    }
    
    return Promise.reject(error)
  }
)

export default request
