<template>
  <div class="login-page">
    <!-- 动态背景 -->
    <DynamicBackground />
    
    <!-- 登录表单 -->
    <div class="login-container">
      <div class="login-panel">
        <h2 class="login-title">欢迎登录</h2>
        
        <!-- 登录方式切换 -->
        <div class="login-tabs">
          <div 
            :class="['tab-item', { active: loginType === 'password' }]" 
            @click="switchLoginType('password')"
          >
            密码登录
          </div>
          <div 
            :class="['tab-item', { active: loginType === 'sms' }]" 
            @click="switchLoginType('sms')"
          >
            验证码登录
          </div>
        </div>
        
        <!-- 密码登录表单 -->
        <el-form 
          v-if="loginType === 'password'"
          ref="passwordFormRef" 
          :model="passwordForm" 
          :rules="passwordRules" 
          label-width="0"
          class="login-form"
        >
          <el-form-item prop="account">
            <el-input 
              v-model="passwordForm.account" 
              placeholder="请输入手机号/邮箱"
              prefix-icon="User"
              size="large"
            />
          </el-form-item>
          
          <el-form-item prop="password">
            <el-input 
              v-model="passwordForm.password" 
              type="password"
              placeholder="请输入密码"
              prefix-icon="Lock"
              size="large"
              show-password
            />
          </el-form-item>
          
          <div class="form-options">
            <el-checkbox v-model="rememberMe">记住我</el-checkbox>
            <a href="#" class="forgot-link">忘记密码？</a>
          </div>
          
          <el-form-item>
            <el-button 
              type="primary" 
              class="login-btn"
              size="large"
              :loading="loading"
              @click="handlePasswordLogin"
            >
              登录
            </el-button>
          </el-form-item>
        </el-form>
        
        <!-- 验证码登录表单 -->
        <el-form 
          v-else
          ref="smsFormRef" 
          :model="smsForm" 
          :rules="smsRules" 
          label-width="0"
          class="login-form"
        >
          <el-form-item prop="phone">
            <el-input 
              v-model="smsForm.phone" 
              placeholder="请输入手机号"
              prefix-icon="Phone"
              size="large"
            />
          </el-form-item>
          
          <el-form-item prop="code">
            <div class="code-input">
              <el-input 
                v-model="smsForm.code" 
                placeholder="请输入验证码"
                prefix-icon="Message"
                size="large"
              />
              <el-button 
                type="primary" 
                :disabled="countdown > 0"
                @click="sendCode"
                size="large"
                class="code-btn"
              >
                {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
              </el-button>
            </div>
          </el-form-item>
          
          <el-form-item>
            <el-button 
              type="primary" 
              class="login-btn"
              size="large"
              :loading="loading"
              @click="handleSmsLogin"
            >
              登录
            </el-button>
          </el-form-item>
        </el-form>
        
        <div class="login-footer">
          <span>还没有账号？</span>
          <router-link to="/register">立即注册</router-link>
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

const router = useRouter()
const userStore = useUserStore()

const loginType = ref('password')
const passwordFormRef = ref(null)
const smsFormRef = ref(null)
const loading = ref(false)
const countdown = ref(0)
const rememberMe = ref(false)

// 密码登录表单
const passwordForm = reactive({
  account: '',
  password: ''
})

// 验证码登录表单
const smsForm = reactive({
  phone: '',
  code: ''
})

// 密码登录验证规则
const passwordRules = {
  account: [
    { required: true, message: '请输入手机号或邮箱', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ]
}

// 验证码登录验证规则
const smsRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码为6位数字', trigger: 'blur' }
  ]
}

// 切换登录方式
const switchLoginType = (type) => {
  loginType.value = type
}

// 发送验证码
const sendCode = async () => {
  try {
    await smsFormRef.value.validateField('phone')
    // TODO: 调用发送验证码API
    ElMessage.success('验证码已发送')
    countdown.value = 60
    const timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(timer)
      }
    }, 1000)
  } catch (error) {
    console.error(error)
  }
}

// 密码登录
const handlePasswordLogin = async () => {
  try {
    await passwordFormRef.value.validate()
    loading.value = true
    
    // 判断账号是手机号还是邮箱
    const account = passwordForm.account
    const isEmail = account.includes('@')
    
    // 调用后端登录API
    // 使用/auth/login接口进行用户登录
    const requestBody = {
      password: passwordForm.password
    }
    
    // 根据输入类型设置phone或email
    if (isEmail) {
      requestBody.email = account
    } else {
      requestBody.phone = account
    }
    
    const response = await fetch('http://localhost:8080/api/auth/admin/login', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(requestBody)
    })
    
    const result = await response.json()
    console.log('登录响应:', result)
    
    if (result.success && result.data) {
      const authResponse = result.data
      
      // 保存token
      userStore.setToken(authResponse.accessToken)
      
      // 构建用户信息对象
      const userInfo = {
        id: authResponse.userId,
        nickname: authResponse.nickname,
        avatar: authResponse.avatar,
        role: authResponse.role
      }
      userStore.setUserInfo(userInfo)
      
      ElMessage.success({
        message: '登录成功',
        duration: 1000
      })
      router.push('/')
    } else {
      ElMessage.error(result.message || '登录失败')
    }
  } catch (error) {
    console.error('登录失败:', error)
    ElMessage.error('登录失败：' + (error.message || '请检查账号密码'))
  } finally {
    loading.value = false
  }
}

// 验证码登录
const handleSmsLogin = async () => {
  try {
    await smsFormRef.value.validate()
    loading.value = true
    
    // TODO: 调用验证码登录API
    // 模拟生成用户ID（实际应该从后端返回）
    const userId = Date.now() % 10000 + 1000 // 生成4位数ID
    
    userStore.setToken('mock-token')
    userStore.setUserInfo({
      id: userId,
      phone: smsForm.phone,
      nickname: '用户' + smsForm.phone.slice(-4),
      avatar: `https://api.dicebear.com/7.x/avataaars/svg?seed=phone${smsForm.phone}`,
      role: 'USER',
      followingIds: []
    })
    
    ElMessage.success({
      message: '登录成功',
      duration: 1000
    })
    router.push('/')
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}
</script>


<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.login-container {
  position: relative;
  z-index: 10;
}

.login-panel {
  width: 420px;
  padding: 50px 40px;
  background: rgba(255, 255, 255, 0.25);
  backdrop-filter: blur(8px);
  border-radius: 24px;
  box-shadow: 0 20px 60px rgba(244, 63, 94, 0.15);
  border: 1px solid rgba(255, 255, 255, 0.5);
}

.login-title {
  text-align: center;
  margin-bottom: 30px;
  color: #2c3e50;
  font-size: 28px;
  font-weight: 700;
  text-shadow: 0 2px 4px rgba(255, 255, 255, 0.8);
}

.login-tabs {
  display: flex;
  margin-bottom: 30px;
  background: rgba(255, 228, 230, 0.4);
  border-radius: 12px;
  padding: 4px;
}

.tab-item {
  flex: 1;
  text-align: center;
  padding: 12px 0;
  cursor: pointer;
  border-radius: 10px;
  font-size: 15px;
  color: #2c3e50;
  transition: all 0.3s;
  font-weight: 600;
}

.tab-item.active {
  background: linear-gradient(135deg, #f43f5e, #fb7185);
  color: #fff;
  box-shadow: 0 4px 15px rgba(244, 63, 94, 0.3);
}

.tab-item:not(.active):hover {
  color: #f43f5e;
}

.login-form {
  width: 100%;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.forgot-link {
  color: #f43f5e;
  font-size: 14px;
  text-decoration: none;
}

.forgot-link:hover {
  text-decoration: underline;
}

.code-input {
  display: flex;
  gap: 12px;
  width: 100%;
}

.code-input .el-input {
  flex: 1;
}

.code-btn {
  width: 120px;
  flex-shrink: 0;
}

.login-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 12px;
}

.login-footer {
  text-align: center;
  margin-top: 30px;
  color: #2c3e50;
  font-size: 14px;
  font-weight: 500;
}

.login-footer a {
  color: #f43f5e;
  text-decoration: none;
  margin-left: 5px;
  font-weight: 500;
}

.login-footer a:hover {
  text-decoration: underline;
}

/* Element Plus 样式覆盖 */
:deep(.el-checkbox__label) {
  color: #2c3e50 !important;
  font-weight: 600;
}

:deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
  background-color: #f43f5e;
  border-color: #f43f5e;
}

:deep(.el-checkbox__input.is-checked + .el-checkbox__label) {
  color: #f43f5e;
}
</style>
