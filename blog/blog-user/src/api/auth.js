import request from '@/utils/request'

// 用户注册
export function register(data) {
  return request.post('/auth/register', data)
}

// 发送短信验证码
export function sendSmsCode(phone) {
  return request.post('/auth/send-sms', { phone })
}

// 用户登录（短信验证码）
export function login(data) {
  return request.post('/auth/login', data)
}

// 密码登录
export function passwordLogin(data) {
  return request.post('/auth/admin/login', data)
}

// 用户登出
export function logout() {
  return request.post('/auth/logout')
}

// 刷新令牌
export function refreshToken() {
  return request.post('/auth/refresh')
}
