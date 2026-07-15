import request from '@/utils/request'

// 获取用户信息
export function getUserProfile() {
  return request.get('/users/profile')
}

// 更新用户信息
export function updateUserProfile(data) {
  return request.put('/users/profile', data)
}

// 上传头像
export function uploadAvatar(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/upload/image', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 申请成为博主
export function applyBlogger(data) {
  return request.post('/users/apply-blogger', data)
}

// 获取博主申请状态
export function getBloggerApplicationStatus() {
  return request.get('/users/blogger-application')
}

// 取消博主申请
export function cancelBloggerApplication() {
  return request.delete('/users/blogger-application')
}

// 获取用户个人空间
export function getUserSpace(userId) {
  return request.get(`/users/${userId}/space`)
}

// 更新个人空间设置
export function updateSpaceSettings(data) {
  return request.put('/users/space/settings', data)
}

// 获取浏览历史
export function getBrowseHistory(params) {
  return request.get('/users/browse-history', { params })
}

// 删除浏览历史
export function deleteBrowseHistory(id) {
  return request.delete(`/users/browse-history/${id}`)
}

// 清空所有浏览历史
export function clearBrowseHistory() {
  return request.delete('/users/browse-history')
}

// 获取购买记录
export function getPurchases(params) {
  return request.get('/users/purchases', { params })
}
