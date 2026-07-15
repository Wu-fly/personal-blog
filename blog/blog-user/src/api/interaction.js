import request from '@/utils/request'

/**
 * 点赞/取消点赞文章
 */
export function toggleLike(articleId) {
  return request({
    url: '/interactions/like',
    method: 'post',
    data: { articleId }
  })
}

/**
 * 收藏/取消收藏文章
 */
export function toggleFavorite(articleId) {
  return request({
    url: '/interactions/favorite',
    method: 'post',
    data: { articleId }
  })
}

/**
 * 获取文章点赞状态
 */
export function getLikeStatus(articleId) {
  return request({
    url: '/interactions/like/status',
    method: 'get',
    params: { articleId }
  })
}

/**
 * 获取文章收藏状态
 */
export function getFavoriteStatus(articleId) {
  return request({
    url: '/interactions/favorite/status',
    method: 'get',
    params: { articleId }
  })
}

/**
 * 关注/取消关注用户
 */
export function toggleFollow(userId) {
  return request({
    url: '/interactions/follow',
    method: 'post',
    data: { userId }
  })
}

/**
 * 打赏博主
 */
export function rewardBlogger(bloggerId, amount, articleId) {
  return request({
    url: '/interactions/reward',
    method: 'post',
    data: { bloggerId, amount, articleId }
  })
}

/**
 * 获取关注状态
 */
export function getFollowStatus(userId) {
  return request({
    url: '/interactions/follow/status',
    method: 'get',
    params: { userId }
  })
}

/**
 * 获取收藏列表
 */
export function getFavoriteList(page = 0, size = 10) {
  return request({
    url: '/interactions/favorites',
    method: 'get',
    params: { page, size }
  })
}

/**
 * 获取点赞列表
 */
export function getLikeList(page = 0, size = 100) {
  return request({
    url: '/interactions/likes',
    method: 'get',
    params: { page, size }
  })
}

/**
 * 获取已购文章列表
 */
export function getPurchaseList(page = 0, size = 100) {
  return request({
    url: '/interactions/purchases',
    method: 'get',
    params: { page, size }
  })
}
