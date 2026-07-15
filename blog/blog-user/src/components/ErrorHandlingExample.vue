<template>
  <div class="error-handling-example">
    <el-card header="错误处理示例">
      <el-space direction="vertical" :size="20" style="width: 100%">
        <!-- 网络状态 -->
        <el-alert
          v-if="!isOnline"
          title="网络已断开"
          type="warning"
          :closable="false"
        />

        <!-- 基本错误处理 -->
        <div>
          <h4>基本错误处理</h4>
          <el-space>
            <el-button @click="testSuccess">成功消息</el-button>
            <el-button @click="testError">错误消息</el-button>
            <el-button @click="testWarning">警告消息</el-button>
            <el-button @click="testInfo">信息消息</el-button>
          </el-space>
        </div>

        <!-- 异步操作错误处理 -->
        <div>
          <h4>异步操作错误处理</h4>
          <el-space>
            <el-button 
              :loading="loading" 
              @click="testAsyncSuccess"
            >
              异步成功
            </el-button>
            <el-button 
              :loading="loading" 
              @click="testAsyncError"
            >
              异步失败
            </el-button>
            <el-button 
              :loading="loading" 
              @click="testNetworkError"
            >
              网络错误
            </el-button>
          </el-space>
        </div>

        <!-- 认证错误 -->
        <div>
          <h4>认证错误处理</h4>
          <el-button @click="testAuthError">
            模拟令牌过期
          </el-button>
        </div>

        <!-- 权限错误 -->
        <div>
          <h4>权限错误处理</h4>
          <el-space>
            <el-button @click="testPermissionError">
              权限不足
            </el-button>
            <el-button @click="testBloggerPermission">
              博主权限不足
            </el-button>
          </el-space>
        </div>

        <!-- 重试机制 -->
        <div>
          <h4>重试机制</h4>
          <el-button 
            :loading="loading" 
            @click="testRetry"
          >
            测试重试（3次）
          </el-button>
        </div>

        <!-- 表单错误 -->
        <div>
          <h4>表单验证错误</h4>
          <el-form>
            <el-form-item 
              label="邮箱" 
              :error="getFieldError('email')"
            >
              <el-input 
                v-model="formData.email"
                @input="clearFieldError('email')"
              />
            </el-form-item>
            <el-form-item 
              label="密码" 
              :error="getFieldError('password')"
            >
              <el-input 
                v-model="formData.password"
                type="password"
                @input="clearFieldError('password')"
              />
            </el-form-item>
            <el-button 
              type="primary" 
              :loading="loading"
              @click="testFormValidation"
            >
              提交表单
            </el-button>
          </el-form>
        </div>

        <!-- 错误信息显示 -->
        <el-alert
          v-if="error"
          :title="errorMessage"
          type="error"
          :closable="true"
          @close="clearError"
        />
      </el-space>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useErrorHandler, useFormErrorHandler, useNetworkStatus } from '@/composables/useErrorHandler'

const { 
  loading, 
  error, 
  errorMessage,
  withErrorHandling,
  clearError,
  retry,
  showSuccess,
  showError,
  showWarning,
  showInfo,
  handleAuthError,
  handlePermissionError
} = useErrorHandler()

const { 
  getFieldError, 
  clearFieldError, 
  setFieldError,
  clearAllErrors 
} = useFormErrorHandler()

const { isOnline } = useNetworkStatus()

const formData = ref({
  email: '',
  password: ''
})

// 测试基本消息
const testSuccess = () => {
  showSuccess('操作成功！')
}

const testError = () => {
  showError('操作失败！')
}

const testWarning = () => {
  showWarning('请注意！')
}

const testInfo = () => {
  showInfo('提示信息')
}

// 测试异步操作
const testAsyncSuccess = async () => {
  await withErrorHandling(async () => {
    await new Promise(resolve => setTimeout(resolve, 1000))
    showSuccess('异步操作成功')
  })
}

const testAsyncError = async () => {
  await withErrorHandling(async () => {
    await new Promise((_, reject) => 
      setTimeout(() => reject(new Error('异步操作失败')), 1000)
    )
  })
}

const testNetworkError = async () => {
  await withErrorHandling(async () => {
    const error = new Error('Network Error')
    error.code = 'ECONNABORTED'
    throw error
  })
}

// 测试认证错误
const testAuthError = () => {
  handleAuthError('登录已过期，请重新登录')
}

// 测试权限错误
const testPermissionError = () => {
  handlePermissionError('您没有权限执行此操作')
}

const testBloggerPermission = () => {
  handlePermissionError('此功能仅限博主使用', 'NOT_BLOGGER')
}

// 测试重试机制
let retryCount = 0
const testRetry = async () => {
  retryCount = 0
  await withErrorHandling(async () => {
    const result = await retry(async () => {
      retryCount++
      console.log(`尝试第 ${retryCount} 次`)
      if (retryCount < 3) {
        throw new Error('操作失败，正在重试...')
      }
      return '成功'
    }, 3, 500)
    
    showSuccess(`重试成功！共尝试 ${retryCount} 次`)
    return result
  })
}

// 测试表单验证
const testFormValidation = async () => {
  clearAllErrors()
  
  await withErrorHandling(async () => {
    // 模拟表单验证错误
    if (!formData.value.email) {
      setFieldError('email', '邮箱不能为空')
    } else if (!formData.value.email.includes('@')) {
      setFieldError('email', '邮箱格式不正确')
    }
    
    if (!formData.value.password) {
      setFieldError('password', '密码不能为空')
    } else if (formData.value.password.length < 6) {
      setFieldError('password', '密码长度不能少于6位')
    }
    
    if (getFieldError('email') || getFieldError('password')) {
      throw new Error('表单验证失败')
    }
    
    // 模拟提交成功
    await new Promise(resolve => setTimeout(resolve, 1000))
    showSuccess('表单提交成功')
  }, {
    showErrorMsg: false // 不显示默认错误消息，使用表单字段错误
  })
}
</script>

<style scoped>
.error-handling-example {
  padding: 20px;
}

h4 {
  margin: 0 0 10px 0;
  color: #303133;
  font-size: 14px;
  font-weight: 600;
}
</style>
