/**
 * 全局错误处理插件
 * 捕获 Vue 应用中的所有未处理错误
 */

import { handleError, showError } from '@/utils/errorHandler'

export default {
  install(app) {
    // 捕获 Vue 组件错误
    app.config.errorHandler = (err, instance, info) => {
      console.error('Vue Error:', err)
      console.error('Error Info:', info)
      console.error('Component:', instance)

      // 开发环境显示详细错误
      if (import.meta.env.DEV) {
        showError(`组件错误: ${err.message}`)
      } else {
        // 生产环境显示友好提示
        showError('页面出现错误，请刷新后重试')
      }

      // 可以在这里上报错误到监控系统
      // reportError(err, instance, info)
    }

    // 捕获 Promise 未处理的 rejection
    window.addEventListener('unhandledrejection', (event) => {
      console.error('Unhandled Promise Rejection:', event.reason)

      // 阻止默认的控制台错误输出
      event.preventDefault()

      // 处理错误
      if (event.reason && typeof event.reason === 'object') {
        handleError(event.reason)
      } else {
        showError('操作失败，请稍后重试')
      }

      // 可以在这里上报错误到监控系统
      // reportError(event.reason)
    })

    // 捕获全局 JavaScript 错误
    window.addEventListener('error', (event) => {
      // 忽略 ResizeObserver 错误（这是浏览器的良性警告）
      if (event.message && event.message.includes('ResizeObserver')) {
        event.preventDefault()
        return
      }

      console.error('Global Error:', event.error)

      // 资源加载错误
      if (event.target !== window) {
        console.error('Resource Load Error:', event.target)
        // 不显示资源加载错误的提示，避免干扰用户
        return
      }

      // JavaScript 运行时错误
      if (import.meta.env.DEV) {
        showError(`运行时错误: ${event.message}`)
      } else {
        showError('页面出现错误，请刷新后重试')
      }

      // 可以在这里上报错误到监控系统
      // reportError(event.error)
    })

    // 提供全局错误处理方法
    app.config.globalProperties.$handleError = handleError
    app.config.globalProperties.$showError = showError
  }
}

/**
 * 错误上报函数（可选）
 * 可以集成 Sentry、Bugsnag 等错误监控服务
 */
function reportError(error, instance, info) {
  // 示例：上报到 Sentry
  // if (window.Sentry) {
  //   window.Sentry.captureException(error, {
  //     contexts: {
  //       vue: {
  //         componentName: instance?.$options?.name,
  //         propsData: instance?.$props,
  //         info: info
  //       }
  //     }
  //   })
  // }

  // 示例：上报到自定义后端
  // fetch('/api/errors/report', {
  //   method: 'POST',
  //   headers: { 'Content-Type': 'application/json' },
  //   body: JSON.stringify({
  //     message: error.message,
  //     stack: error.stack,
  //     component: instance?.$options?.name,
  //     info: info,
  //     userAgent: navigator.userAgent,
  //     url: window.location.href,
  //     timestamp: new Date().toISOString()
  //   })
  // }).catch(console.error)
}
