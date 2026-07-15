/**
 * 统一错误处理工具
 * 提供错误分类、错误消息映射和错误处理策略
 */

import { ElMessage, ElNotification } from 'element-plus'
import router from '@/router'

/**
 * 错误类型枚举
 */
export const ErrorType = {
  NETWORK: 'NETWORK',           // 网络错误
  AUTH: 'AUTH',                 // 认证错误
  PERMISSION: 'PERMISSION',     // 权限错误
  VALIDATION: 'VALIDATION',     // 验证错误
  BUSINESS: 'BUSINESS',         // 业务逻辑错误
  SERVER: 'SERVER',             // 服务器错误
  TIMEOUT: 'TIMEOUT',           // 超时错误
  UNKNOWN: 'UNKNOWN'            // 未知错误
}

/**
 * 错误消息映射
 */
const errorMessages = {
  // 网络错误
  'ERR_NETWORK': '网络连接失败，请检查网络设置',
  'ERR_INTERNET_DISCONNECTED': '网络已断开，请检查网络连接',
  'ECONNABORTED': '请求超时，请稍后重试',
  
  // 认证错误
  'TOKEN_EXPIRED': '登录已过期，请重新登录',
  'TOKEN_INVALID': '登录凭证无效，请重新登录',
  'UNAUTHORIZED': '未登录或登录已过期',
  
  // 权限错误
  'FORBIDDEN': '您没有权限执行此操作',
  'NOT_BLOGGER': '此功能仅限博主使用，请先申请成为博主',
  'NOT_ADMIN': '此功能仅限管理员使用',
  
  // 验证错误
  'VALIDATION_ERROR': '输入信息有误，请检查后重试',
  'PHONE_EXISTS': '该手机号已被注册',
  'EMAIL_EXISTS': '该邮箱已被注册',
  'INVALID_PHONE': '手机号格式不正确',
  'INVALID_EMAIL': '邮箱格式不正确',
  'INVALID_PASSWORD': '密码格式不正确',
  'INVALID_SMS_CODE': '验证码错误或已过期',
  
  // 业务逻辑错误
  'INSUFFICIENT_BALANCE': '余额不足，请先充值',
  'ARTICLE_NOT_FOUND': '文章不存在或已被删除',
  'COMMENT_NOT_FOUND': '评论不存在或已被删除',
  'USER_NOT_FOUND': '用户不存在',
  'ALREADY_LIKED': '您已经点赞过了',
  'ALREADY_FAVORITED': '您已经收藏过了',
  'ALREADY_FOLLOWED': '您已经关注过了',
  'ALREADY_PURCHASED': '您已经购买过该文章',
  'SENSITIVE_WORD_DETECTED': '内容包含敏感词，请修改后重试',
  'ARTICLE_NOT_APPROVED': '文章未通过审核，暂时无法查看',
  
  // 服务器错误
  'INTERNAL_SERVER_ERROR': '服务器内部错误，请稍后重试',
  'SERVICE_UNAVAILABLE': '服务暂时不可用，请稍后重试',
  'DATABASE_ERROR': '数据库错误，请联系管理员',
  
  // 限流错误
  'RATE_LIMIT_EXCEEDED': '操作过于频繁，请稍后再试',
  'SMS_LIMIT_EXCEEDED': '短信发送次数已达上限，请稍后再试'
}

/**
 * 根据错误码获取友好的错误消息
 */
export function getErrorMessage(errorCode, defaultMessage = '操作失败，请稍后重试') {
  return errorMessages[errorCode] || defaultMessage
}

/**
 * 根据HTTP状态码判断错误类型
 */
export function getErrorType(status, errorCode) {
  if (!status) {
    return ErrorType.NETWORK
  }
  
  if (status === 401) {
    return ErrorType.AUTH
  }
  
  if (status === 403) {
    return ErrorType.PERMISSION
  }
  
  if (status === 400 || status === 422) {
    return ErrorType.VALIDATION
  }
  
  if (status >= 500) {
    return ErrorType.SERVER
  }
  
  if (errorCode === 'ECONNABORTED') {
    return ErrorType.TIMEOUT
  }
  
  return ErrorType.BUSINESS
}

/**
 * 显示错误通知
 */
export function showError(message, type = 'error', duration = 3000) {
  ElMessage({
    message,
    type,
    duration,
    showClose: true
  })
}

/**
 * 显示错误通知（详细版，使用 Notification）
 */
export function showErrorNotification(title, message, duration = 4500) {
  ElNotification({
    title,
    message,
    type: 'error',
    duration,
    position: 'top-right'
  })
}

/**
 * 显示成功消息
 */
export function showSuccess(message, duration = 2000) {
  ElMessage({
    message,
    type: 'success',
    duration,
    showClose: true
  })
}

/**
 * 显示警告消息
 */
export function showWarning(message, duration = 3000) {
  ElMessage({
    message,
    type: 'warning',
    duration,
    showClose: true
  })
}

/**
 * 显示信息消息
 */
export function showInfo(message, duration = 2000) {
  ElMessage({
    message,
    type: 'info',
    duration,
    showClose: true
  })
}

/**
 * 处理认证错误
 */
import { getItem, setItem, clearAuth } from './storage'

export function handleAuthError(message = '登录已过期，请重新登录') {
  // 清除认证信息
  clearAuth()
  
  // 显示错误消息
  showError(message)
  
  // 跳转到登录页，并保存当前路径用于登录后跳转
  const currentPath = router.currentRoute.value.fullPath
  if (currentPath !== '/login') {
    router.push({
      path: '/login',
      query: { redirect: currentPath }
    })
  }
}

/**
 * 处理权限错误
 */
export function handlePermissionError(message = '您没有权限执行此操作', errorCode) {
  // 如果是博主权限错误，提示申请成为博主
  if (errorCode === 'NOT_BLOGGER') {
    showErrorNotification(
      '权限不足',
      '此功能仅限博主使用，请先申请成为博主'
    )
    // 可以选择跳转到博主申请页面
    // router.push('/apply-blogger')
  } else {
    showError(message)
  }
}

/**
 * 处理网络错误
 */
export function handleNetworkError(error) {
  if (error.code === 'ECONNABORTED') {
    showError('请求超时，请检查网络连接')
  } else if (error.message === 'Network Error') {
    showError('网络连接失败，请检查网络设置')
  } else {
    showError('网络异常，请稍后重试')
  }
}

/**
 * 处理业务逻辑错误
 */
export function handleBusinessError(errorCode, message) {
  const friendlyMessage = getErrorMessage(errorCode, message)
  showError(friendlyMessage)
}

/**
 * 处理服务器错误
 */
export function handleServerError(status, message) {
  if (status >= 500 && status < 600) {
    showErrorNotification(
      '服务器错误',
      message || '服务器出现问题，请稍后重试'
    )
  }
}

/**
 * 统一错误处理入口
 */
export function handleError(error) {
  // 如果错误已经被处理过，直接返回
  if (error.handled) {
    return
  }
  
  const { response, code, message } = error
  
  if (response) {
    const { status, data } = response
    const errorCode = data?.errorCode || data?.code
    const errorMessage = data?.message || message
    
    const errorType = getErrorType(status, errorCode)
    
    switch (errorType) {
      case ErrorType.AUTH:
        handleAuthError(errorMessage)
        break
      case ErrorType.PERMISSION:
        handlePermissionError(errorMessage, errorCode)
        break
      case ErrorType.VALIDATION:
      case ErrorType.BUSINESS:
        handleBusinessError(errorCode, errorMessage)
        break
      case ErrorType.SERVER:
        handleServerError(status, errorMessage)
        break
      default:
        showError(errorMessage || '操作失败，请稍后重试')
    }
  } else if (code === 'ECONNABORTED' || message === 'Network Error') {
    handleNetworkError(error)
  } else {
    showError(message || '未知错误，请稍后重试')
  }
  
  // 标记错误已处理
  error.handled = true
}

/**
 * 创建错误对象
 */
export function createError(type, message, code) {
  const error = new Error(message)
  error.type = type
  error.code = code
  return error
}

/**
 * 表单验证错误处理
 */
export function handleValidationErrors(errors) {
  if (Array.isArray(errors)) {
    errors.forEach(error => {
      showError(error.message || error)
    })
  } else if (typeof errors === 'object') {
    Object.values(errors).forEach(error => {
      showError(error)
    })
  } else {
    showError(errors || '表单验证失败')
  }
}

export default {
  ErrorType,
  getErrorMessage,
  getErrorType,
  showError,
  showErrorNotification,
  showSuccess,
  showWarning,
  showInfo,
  handleAuthError,
  handlePermissionError,
  handleNetworkError,
  handleBusinessError,
  handleServerError,
  handleError,
  createError,
  handleValidationErrors
}
