<template>
  <teleport to="body">
    <transition name="fade">
      <div v-if="visible" class="error-notification" :class="`error-notification--${type}`">
        <div class="error-notification__icon">
          <el-icon :size="24">
            <CircleClose v-if="type === 'error'" />
            <WarningFilled v-if="type === 'warning'" />
            <InfoFilled v-if="type === 'info'" />
            <SuccessFilled v-if="type === 'success'" />
          </el-icon>
        </div>
        <div class="error-notification__content">
          <div class="error-notification__title">{{ title }}</div>
          <div v-if="message" class="error-notification__message">{{ message }}</div>
        </div>
        <div class="error-notification__close" @click="close">
          <el-icon :size="16">
            <Close />
          </el-icon>
        </div>
      </div>
    </transition>
  </teleport>
</template>

<script setup>
import { ref, watch } from 'vue'
import { ElIcon } from 'element-plus'
import { CircleClose, WarningFilled, InfoFilled, SuccessFilled, Close } from '@element-plus/icons-vue'

const props = defineProps({
  type: {
    type: String,
    default: 'error',
    validator: (value) => ['error', 'warning', 'info', 'success'].includes(value)
  },
  title: {
    type: String,
    required: true
  },
  message: {
    type: String,
    default: ''
  },
  duration: {
    type: Number,
    default: 4500
  },
  show: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['close'])

const visible = ref(props.show)
let timer = null

watch(() => props.show, (newVal) => {
  visible.value = newVal
  if (newVal && props.duration > 0) {
    clearTimeout(timer)
    timer = setTimeout(() => {
      close()
    }, props.duration)
  }
})

const close = () => {
  visible.value = false
  clearTimeout(timer)
  emit('close')
}
</script>

<style scoped>
.error-notification {
  position: fixed;
  top: 20px;
  right: 20px;
  min-width: 330px;
  max-width: 450px;
  padding: 16px 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  display: flex;
  align-items: flex-start;
  gap: 12px;
  z-index: 9999;
  transition: all 0.3s ease;
}

.error-notification--error {
  border-left: 4px solid #f56c6c;
}

.error-notification--warning {
  border-left: 4px solid #e6a23c;
}

.error-notification--info {
  border-left: 4px solid #909399;
}

.error-notification--success {
  border-left: 4px solid #67c23a;
}

.error-notification__icon {
  flex-shrink: 0;
  margin-top: 2px;
}

.error-notification--error .error-notification__icon {
  color: #f56c6c;
}

.error-notification--warning .error-notification__icon {
  color: #e6a23c;
}

.error-notification--info .error-notification__icon {
  color: #909399;
}

.error-notification--success .error-notification__icon {
  color: #67c23a;
}

.error-notification__content {
  flex: 1;
  min-width: 0;
}

.error-notification__title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
  line-height: 1.4;
}

.error-notification__message {
  font-size: 14px;
  color: #606266;
  line-height: 1.5;
  word-wrap: break-word;
}

.error-notification__close {
  flex-shrink: 0;
  cursor: pointer;
  color: #909399;
  transition: color 0.2s;
  margin-top: 2px;
}

.error-notification__close:hover {
  color: #606266;
}

.fade-enter-active,
.fade-leave-active {
  transition: all 0.3s ease;
}

.fade-enter-from {
  opacity: 0;
  transform: translateX(100%);
}

.fade-leave-to {
  opacity: 0;
  transform: translateX(100%);
}
</style>
