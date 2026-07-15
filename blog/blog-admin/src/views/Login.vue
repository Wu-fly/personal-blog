<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <div class="card-header">
          <h2>博客管理后台</h2>
        </div>
      </template>
      
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="rules"
        label-width="80px"
      >
        <el-form-item label="账号" prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入手机号或邮箱"
            clearable
          />
        </el-form-item>
        
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            clearable
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button
            type="primary"
            :loading="loading"
            style="width: 100%"
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAdminStore } from '../stores/admin'
import { ElMessage } from 'element-plus'
import request from '../utils/request'

const router = useRouter()
const adminStore = useAdminStore()

const loginFormRef = ref(null)
const loading = ref(false)

const loginForm = reactive({
  username: '13900000000',  // 使用管理员手机号
  password: 'admin123'      // 使用正确的密码
})

const rules = {
  username: [
    { required: true, message: '请输入手机号或邮箱', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  await loginFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    loading.value = true
    try {
      // 清除旧的token和用户信息
      adminStore.logout()
      
      // 使用手机号或邮箱登录
      // 判断输入的是手机号还是邮箱
      const isEmail = loginForm.username.includes('@')
      const loginData = isEmail 
        ? { email: loginForm.username, password: loginForm.password }
        : { phone: loginForm.username, password: loginForm.password }
      
      console.log('发送登录请求:', loginData)
      const res = await request.post('/auth/admin/login', loginData)
      
      console.log('登录响应:', res)
      
      // 检查响应数据
      if (!res.data) {
        ElMessage.error('登录响应数据异常')
        return
      }
      
      // 检查是否是管理员
      if (res.data.role !== 'ADMIN') {
        ElMessage.error('您没有管理员权限')
        return
      }
      
      // 保存token和用户信息
      adminStore.setToken(res.data.accessToken)
      adminStore.setAdminInfo(res.data)
      
      ElMessage.success('登录成功')
      
      // 跳转到仪表盘
      router.push('/dashboard')
    } catch (error) {
      console.error('登录失败:', error)
      ElMessage.error(error.message || '登录失败')
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  width: 400px;
}

.card-header {
  text-align: center;
}

.card-header h2 {
  margin: 0;
  color: #303133;
}
</style>
