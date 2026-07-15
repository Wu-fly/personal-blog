import request from '@/utils/request'

// 发送私信
export function sendMessage(data) {
  return request.post('/messages', data)
}

// 获取收件箱
export function getInbox(params) {
  return request.get('/messages/inbox', { params })
}

// 获取发件箱
export function getOutbox(params) {
  return request.get('/messages/outbox', { params })
}

// 获取对话记录
export function getConversation(userId, params) {
  return request.get(`/messages/conversation/${userId}`, { params })
}

// 标记已读
export function markAsRead(messageId) {
  return request.put(`/messages/${messageId}/read`)
}

// 获取未读消息数
export function getUnreadCount() {
  return request.get('/messages/unread-count')
}