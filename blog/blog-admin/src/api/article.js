import request from '../utils/request'

// 获取文章列表
export function getArticleList(params) {
  return request({
    url: '/articles',
    method: 'get',
    params
  })
}
