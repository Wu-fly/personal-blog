import request from '@/utils/request'

// 获取文章列表
export function getArticles(params) {
  return request.get('/articles', { params })
}

// 获取文章详情
export function getArticleDetail(id, forEdit = false) {
  return request.get(`/articles/${id}`, { params: { forEdit } })
}

// 创建文章
export function createArticle(data) {
  return request.post('/articles', data)
}

// 更新文章
export function updateArticle(id, data) {
  return request.put(`/articles/${id}`, data)
}

// 删除文章
export function deleteArticle(id) {
  return request.delete(`/articles/${id}`)
}

// 置顶文章
export function pinArticle(id) {
  return request.post(`/articles/${id}/pin`)
}

// 取消置顶
export function unpinArticle(id) {
  return request.delete(`/articles/${id}/pin`)
}

// 获取热门文章（轮播图）
export function getHotArticles() {
  return request.get('/articles/hot')
}

// 搜索文章
export function searchArticles(keyword, params) {
  return request.get('/articles/search', { params: { keyword, ...params } })
}

// 获取分类列表
export function getCategories() {
  return request.get('/categories')
}

// 获取标签列表
export function getTags() {
  return request.get('/tags')
}

// 获取当前用户的文章列表
export function getMyArticles(params) {
  return request.get('/articles/my', { params })
}

// 获取指定用户的文章列表
export function getUserArticles(userId, params) {
  return request.get('/articles', { params: { userId, ...params } })
}

// 记录浏览历史
export function recordBrowseHistory(articleId) {
  return request.post(`/articles/${articleId}/browse`)
}
