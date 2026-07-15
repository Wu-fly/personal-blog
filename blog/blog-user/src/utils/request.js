import axios from 'axios'
import { ElLoading } from 'element-plus'
import router from '@/router'
import { 
  handleError, 
  handleAuthError, 
  showError 
} from './errorHandler'

// 创建axios实例
const request = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 加载状态
let loadingInstance = null
let loadingCount = 0

// 显示加载
function showLoading() {
  if (loadingCount === 0) {
    loadingInstance = ElLoading.service({
      lock: true,
      text: '加载中...',
      background: 'rgba(255, 255, 255, 0.8)'
    })
  }
  loadingCount++
}

// 隐藏加载
function hideLoading() {
  loadingCount--
  if (loadingCount <= 0) {
    loadingCount = 0
    if (loadingInstance) {
      loadingInstance.close()
      loadingInstance = null
    }
  }
}

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    // 显示加载状态（可通过config.showLoading控制）
    if (config.showLoading !== false) {
      showLoading()
    }
    
    // 添加token（从 sessionStorage 读取，避免循环依赖问题）
    const token = sessionStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    
    return config
  },
  (error) => {
    hideLoading()
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    hideLoading()
    
    const { data } = response
    
    // 统一处理响应格式
    // 后端返回格式: { success: boolean, data: any, message: string, code: number }
    if (data.success === false) {
      showError(data.message || '请求失败')
      return Promise.reject(new Error(data.message || '请求失败'))
    }
    
    // 返回数据部分
    return data.data !== undefined ? data.data : data
  },
  (error) => {
    hideLoading()
    
    const { response } = error
    
    // 使用统一错误处理
    if (response) {
      const { status, data } = response
      
      // 特殊处理认证错误
      if (status === 401) {
        handleAuthError(data?.message || '登录已过期，请重新登录')
        return Promise.reject(error)
      }
      
      // 422 业务逻辑错误 - 只显示友好的错误消息，不显示技术性错误
      if (status === 422) {
        const errorMessage = data?.message || '操作失败'
        showError(errorMessage)
        // 创建一个新的错误对象，包含友好的错误消息
        const businessError = new Error(errorMessage)
        businessError.handled = true
        return Promise.reject(businessError)
      }
      
      // 其他错误使用统一处理
      handleError(error)
    } else {
      // 网络错误或超时
      if (error.code === 'ECONNABORTED') {
        showError('请求超时，请检查网络')
      } else if (error.message === 'Network Error') {
        showError('网络连接失败，请检查网络')
      } else {
        showError('网络异常，请稍后重试')
      }
    }
    
    return Promise.reject(error)
  }
)

// 封装 GET 请求（不显示 loading）
export function get(url, params, config = {}) {
  return request.get(url, { params, showLoading: false, ...config })
}

// 封装 POST 请求
export function post(url, data, config = {}) {
  return request.post(url, data, config)
}

// 封装 PUT 请求
export function put(url, data, config = {}) {
  return request.put(url, data, config)
}

// 封装 DELETE 请求
export function del(url, config = {}) {
  return request.delete(url, config)
}

export default request
