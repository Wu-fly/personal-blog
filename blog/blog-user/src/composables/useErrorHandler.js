/**
 * 错误处理 Composable
 * 提供组件级别的错误处理功能
 */

import { ref } from 'vue'
import {
  handleError,
  handleAuthError,
  handlePermissionError,
  handleNetworkError,
  handleBusinessError,
  showError,
  showSuccess,
  showWarning,
  showInfo,
  showErrorNotification
} from '@/utils/errorHandler'

export function useErrorHandler() {
  const loading = ref(false)
  const error = ref(null)
  const errorMessage = ref('')

  /**
   * 包装异步函数，自动处理错误和加载状态
   */
  const withErrorHandling = async (fn, options = {}) => {
    const {
      showLoading = true,
      showErrorMsg = true,
      onError = null,
      onFinally = null
    } = options

    try {
      if (showLoading) {
        loading.value = true
      }
      error.value = null
      errorMessage.value = ''

      const result = await fn()
      return result
    } catch (err) {
      error.value = err
      errorMessage.value = err.message || '操作失败'

      if (showErrorMsg) {
        handleError(err)
      }

      if (onError) {
        onError(err)
      }

      throw err
    } finally {
      if (showLoading) {
        loading.value = false
      }

      if (onFinally) {
        onFinally()
      }
    }
  }

  /**
   * 清除错误状态
   */
  const clearError = () => {
    error.value = null
    errorMessage.value = ''
  }

  /**
   * 重试函数
   */
  const retry = async (fn, maxRetries = 3, delay = 1000) => {
    let lastError = null

    for (let i = 0; i < maxRetries; i++) {
      try {
        return await fn()
      } catch (err) {
        lastError = err
        if (i < maxRetries - 1) {
          await new Promise(resolve => setTimeout(resolve, delay))
        }
      }
    }

    throw lastError
  }

  return {
    loading,
    error,
    errorMessage,
    withErrorHandling,
    clearError,
    retry,
    // 导出错误处理函数
    handleError,
    handleAuthError,
    handlePermissionError,
    handleNetworkError,
    handleBusinessError,
    showError,
    showSuccess,
    showWarning,
    showInfo,
    showErrorNotification
  }
}

/**
 * 表单错误处理 Composable
 */
export function useFormErrorHandler() {
  const formErrors = ref({})

  /**
   * 设置表单字段错误
   */
  const setFieldError = (field, message) => {
    formErrors.value[field] = message
  }

  /**
   * 清除表单字段错误
   */
  const clearFieldError = (field) => {
    delete formErrors.value[field]
  }

  /**
   * 清除所有表单错误
   */
  const clearAllErrors = () => {
    formErrors.value = {}
  }

  /**
   * 从后端响应设置表单错误
   */
  const setErrorsFromResponse = (errors) => {
    if (Array.isArray(errors)) {
      errors.forEach(error => {
        if (error.field) {
          setFieldError(error.field, error.message)
        }
      })
    } else if (typeof errors === 'object') {
      Object.keys(errors).forEach(field => {
        setFieldError(field, errors[field])
      })
    }
  }

  /**
   * 获取字段错误消息
   */
  const getFieldError = (field) => {
    return formErrors.value[field]
  }

  /**
   * 检查字段是否有错误
   */
  const hasFieldError = (field) => {
    return !!formErrors.value[field]
  }

  return {
    formErrors,
    setFieldError,
    clearFieldError,
    clearAllErrors,
    setErrorsFromResponse,
    getFieldError,
    hasFieldError
  }
}

/**
 * 网络状态监听 Composable
 */
export function useNetworkStatus() {
  const isOnline = ref(navigator.onLine)

  const updateOnlineStatus = () => {
    isOnline.value = navigator.onLine
    if (!isOnline.value) {
      showWarning('网络连接已断开')
    } else {
      showSuccess('网络连接已恢复')
    }
  }

  // 监听网络状态变化
  window.addEventListener('online', updateOnlineStatus)
  window.addEventListener('offline', updateOnlineStatus)

  return {
    isOnline
  }
}

export default useErrorHandler
