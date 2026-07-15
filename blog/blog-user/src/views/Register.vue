<template>
  <div class="register-page">
    <!-- 动态背景 -->
    <DynamicBackground />
    
    <!-- 注册表单 -->
    <div class="register-container">
      <div class="register-panel">
        <h2 class="register-title">用户注册</h2>
        
        <el-form 
          ref="formRef" 
          :model="form" 
          :rules="rules" 
          label-width="0"
          class="register-form"
        >
          <el-form-item prop="phone">
            <el-input 
              v-model="form.phone" 
              placeholder="请输入手机号"
              prefix-icon="Phone"
              size="large"
            />
          </el-form-item>
          
          <el-form-item prop="email">
            <el-input 
              v-model="form.email" 
              placeholder="请输入邮箱"
              prefix-icon="Message"
              size="large"
            />
          </el-form-item>
          
          <el-form-item prop="password">
            <el-input 
              v-model="form.password" 
              type="password"
              placeholder="请输入密码"
              prefix-icon="Lock"
              size="large"
              show-password
            />
          </el-form-item>
          
          <el-form-item prop="confirmPassword">
            <el-input 
              v-model="form.confirmPassword" 
              type="password"
              placeholder="请确认密码"
              prefix-icon="Lock"
              size="large"
              show-password
            />
          </el-form-item>
          
          <el-form-item prop="code">
            <div class="code-input">
              <el-input 
                v-model="form.code" 
                placeholder="请输入验证码"
                prefix-icon="Key"
                size="large"
              />
              <el-button 
                type="primary" 
                :disabled="countdown > 0"
                @click="sendCode"
                size="large"
              >
                {{ countdown > 0 ? `${countdown}秒后重发` : '获取验证码' }}
              </el-button>
            </div>
          </el-form-item>
          
          <el-form-item>
            <el-button 
              type="primary" 
              class="register-btn"
              size="large"
              :loading="loading"
              @click="handleRegister"
            >
              注册
            </el-button>
          </el-form-item>
        </el-form>
        
        <div class="register-footer">
          <span>已有账号？</span>
          <router-link to="/login">立即登录</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import DynamicBackground from '@/components/DynamicBackground.vue'
import { register, sendSmsCode } from '@/api/auth'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref(null)
const loading = ref(false)
const countdown = ref(0)

const form = reactive({
  phone: '',
  email: '',
  password: '',
  confirmPassword: '',
  code: ''
})

// 确认密码验证
const validateConfirmPassword = (rule, value, callback) => {
  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

// 密码强度验证
const validatePassword = (rule, value, callback) => {
  if (value.length < 6 || value.length > 20) {
    callback(new Error('密码长度必须为6-20位'))
  } else if (!/[A-Z]/.test(value)) {
    callback(new Error('密码必须包含大写字母'))
  } else if (!/[a-z]/.test(value)) {
    callback(new Error('密码必须包含小写字母'))
  } else if (!/[0-9]/.test(value)) {
    callback(new Error('密码必须包含数字'))
  } else {
    callback()
  }
}

const rules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { validator: validatePassword, trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { min: 6, max: 6, message: '验证码为6位数字', trigger: 'blur' }
  ]
}

// 发送验证码
const sendCode = async () => {
  try {
    await formRef.value.validateField(['phone', 'email'])
    
    // 测试环境:直接显示固定验证码,不调用API
    ElMessage.success('验证码: 123456 (测试环境固定验证码)')
    
    countdown.value = 60
    const timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(timer)
      }
    }, 1000)
  } catch (error) {
    console.error('验证失败:', error)
  }
}

// 注册
const handleRegister = async () => {
  try {
    await formRef.value.validate()
    loading.value = true
    
    // 调用注册API
    const data = await register({
      phone: form.phone,
      email: form.email,
      password: form.password,
      smsCode: form.code
    })
    
    // 保存用户信息和token
    if (data) {
      const { accessToken, userId, nickname, role, email, phone } = data
      userStore.setToken(accessToken)
      userStore.setUserInfo({
        id: userId,
        phone: phone,
        email: email,
        nickname: nickname,
        role: role
      })
      
      ElMessage.success('注册成功')
      router.push('/')
    }
  } catch (error) {
    console.error('注册失败:', error)
    ElMessage.error(error.response?.data?.message || '注册失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.register-container {
  position: relative;
  z-index: 10;
}

.register-panel {
  width: 420px;
  padding: 50px 40px;
  background: rgba(255, 255, 255, 0.25);
  backdrop-filter: blur(8px);
  border-radius: 24px;
  box-shadow: 0 20px 60px rgba(244, 63, 94, 0.15);
  border: 1px solid rgba(255, 255, 255, 0.5);
}

.register-title {
  text-align: center;
  margin-bottom: 40px;
  color: #2c3e50;
  font-size: 28px;
  font-weight: 700;
  text-shadow: 0 2px 4px rgba(255, 255, 255, 0.8);
}

.register-form {
  width: 100%;
}

.code-input {
  display: flex;
  gap: 12px;
  width: 100%;
}

.code-input .el-input {
  flex: 1;
}

.register-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 12px;
}

.register-footer {
  text-align: center;
  margin-top: 30px;
  color: #2c3e50;
  font-size: 14px;
  font-weight: 500;
}

.register-footer a {
  color: #f43f5e;
  text-decoration: none;
  margin-left: 5px;
  font-weight: 500;
}

.register-footer a:hover {
  text-decoration: underline;
}
</style>
