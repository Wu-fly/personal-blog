import request from '@/utils/request'

// 获取文章评论
export function getComments(articleId, params) {
  return request.get(`/comments/article/${articleId}`, { params })
}

// 发表评论
export function createComment(data) {
  return request.post('/comments', data)
}

// 回复评论
export function replyComment(commentId, data) {
  return request.post(`/comments/${commentId}/reply`, data)
}

// 删除评论
export function deleteComment(id) {
  return request.delete(`/comments/${id}`)
}

// 审核评论（博主）
export function approveComment(id, approved) {
  return request.put(`/comments/${id}/approve`, { approved })
}
