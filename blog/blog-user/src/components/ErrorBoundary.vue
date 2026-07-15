<template>
  <div v-if="hasError" class="error-boundary">
    <el-result
      icon="error"
      :title="errorTitle"
      :sub-title="errorMessage"
    >
      <template #extra>
        <el-space>
          <el-button type="primary" @click="handleRetry">
            重试
          </el-button>
          <el-button @click="handleGoHome">
            返回首页
          </el-button>
        </el-space>
      </template>
    </el-result>
  </div>
  <slot v-else></slot>
</template>

<script setup>
import { ref, onErrorCaptured } from 'vue'
import { useRouter } from 'vue-router'
import { ElResult, ElButton, ElSpace } from 'element-plus'

const router = useRouter()

const hasError = ref(false)
const errorTitle = ref('出错了')
const errorMessage = ref('页面加载失败，请稍后重试')

// 捕获子组件错误
onErrorCaptured((err, instance, info) => {
  console.error('ErrorBoundary caught error:', err, info)
  
  hasError.value = true
  errorTitle.value = '页面出错了'
  errorMessage.value = err.message || '页面加载失败，请稍后重试'
  
  // 返回 false 阻止错误继续向上传播
  return false
})

// 重试
const handleRetry = () => {
  hasError.value = false
  errorTitle.value = '出错了'
  errorMessage.value = '页面加载失败，请稍后重试'
  
  // 刷新当前页面
  router.go(0)
}

// 返回首页
const handleGoHome = () => {
  hasError.value = false
  router.push('/')
}
</script>

<style scoped>
.error-boundary {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
  padding: 40px 20px;
}
</style>
