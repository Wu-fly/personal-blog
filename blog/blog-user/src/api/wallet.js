import request from '@/utils/request'

// 获取钱包余额
export function getWalletBalance() {
  return request.get('/wallet/balance')
}

// 充值
export function recharge(data) {
  return request.post('/wallet/recharge', data)
}

// 提现
export function withdraw(data) {
  return request.post('/wallet/withdraw', data)
}

// 获取交易记录
export function getTransactions(params) {
  return request.get('/wallet/transactions', { params })
}

// 获取收益明细
export function getRevenueDetails(params) {
  return request.get('/wallet/revenue', { params })
}

// 购买付费文章
export function purchaseArticle(data) {
  return request.post('/wallet/purchase', data)
}

// 打赏博主
export function reward(data) {
  return request.post('/wallet/reward', data)
}
