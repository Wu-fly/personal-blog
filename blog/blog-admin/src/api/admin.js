import request from '../utils/request'

// 获取平台统计数据
export function getStatistics() {
  return request({
    url: '/admin/statistics',
    method: 'get'
  })
}

// 获取图表数据
export function getChartData() {
  return request({
    url: '/admin/charts',
    method: 'get'
  })
}

// 获取待审核文章列表
export function getPendingArticles(params) {
  return request({
    url: '/admin/articles/pending',
    method: 'get',
    params
  })
}

// 审核文章
export function reviewArticle(id, data) {
  return request({
    url: `/admin/articles/${id}/review`,
    method: 'put',
    data
  })
}

// 获取用户列表
export function getUserList(params) {
  return request({
    url: '/admin/users',
    method: 'get',
    params
  })
}

// 更新用户状态
export function updateUserStatus(id, data) {
  return request({
    url: `/admin/users/${id}/status`,
    method: 'put',
    data
  })
}

// 获取轮播图配置
export function getCarouselConfig() {
  return request({
    url: '/admin/carousel',
    method: 'get'
  })
}

// 更新轮播图配置
export function updateCarouselConfig(data) {
  return request({
    url: '/admin/carousel',
    method: 'put',
    data
  })
}

// 获取博主申请列表
export function getBloggerApplications(params) {
  return request({
    url: '/admin/blogger-applications',
    method: 'get',
    params
  })
}

// 审核博主申请
export function reviewBloggerApplication(id, data) {
  return request({
    url: `/admin/blogger-applications/${id}`,
    method: 'put',
    data
  })
}

// 获取平台钱包信息
export function getAdminWallet() {
  return request({
    url: '/admin/wallet',
    method: 'get'
  })
}

// 获取平台收益明细
export function getAdminRevenue(params) {
  return request({
    url: '/admin/wallet/revenue',
    method: 'get',
    params
  })
}

// 平台提现
export function adminWithdraw(data) {
  return request({
    url: '/admin/wallet/withdraw',
    method: 'post',
    data
  })
}
